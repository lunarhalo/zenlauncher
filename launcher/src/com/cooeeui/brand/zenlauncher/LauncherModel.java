/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cooeeui.brand.zenlauncher;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.cooeeui.brand.zenlauncher.apps.AllAppsList;
import com.cooeeui.brand.zenlauncher.apps.AppFilter;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.FastBitmapDrawable;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.apps.Utilities;
import com.cooeeui.brand.zenlauncher.debug.Logger;
import com.cooeeui.brand.zenlauncher.receivers.InstallShortcutReceiver;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should
 * be only one LauncherModel object held in a static. Also provide APIs for
 * updating the database state for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
    static final boolean DEBUG_LOADERS = true;
    static final String TAG = "Launcher.Model";

    private static final int ITEMS_CHUNK = 6; // batch size for the workspace
                                              // icons
    private final boolean mAppsCanBeOnRemoveableStorage;

    private final LauncherAppState mApp;
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;
    private boolean mIsLoaderTaskRunning;
    private volatile boolean mFlushingWorkerThread;

    // Specific runnable types that are run on the main thread deferred handler,
    // this allows us to clear all queued binding runnables when the Launcher
    // activity is destroyed.
    private static final int MAIN_THREAD_NORMAL_RUNNABLE = 0;
    private static final int MAIN_THREAD_BINDING_RUNNABLE = 1;

    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    // We start off with everything not loaded. After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery. These are only ever touched from the loader thread.
    private boolean mWorkspaceLoaded;
    private boolean mAllAppsLoaded;

    // When we are loading pages synchronously, we can't just post the binding
    // of items on the side
    // pages as this delays the rotation process. Instead, we wait for a
    // callback from the first
    // draw (in Workspace) to initiate the binding of the remaining side pages.
    // Any time we start
    // a normal load, we also clear this set of Runnables.
    static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<Runnable>();

    private WeakReference<Callbacks> mCallbacks;

    // < only access in worker thread >
    AllAppsList mBgAllAppsList;

    // The lock that must be acquired before referencing any static bg data
    // structures. Unlike other locks, this one can generally be held long-term
    // because we never expect any of these static data structures to be
    // referenced outside of the worker thread except on the first load after
    // configuration change.
    static final Object sBgLock = new Object();

    // sBgItemsIdMap maps *all* the ItemInfos (shortcuts, apps)
    // created by LauncherModel to their ids
    static final HashMap<Long, ItemInfo> sBgItemsIdMap = new HashMap<Long, ItemInfo>();

    // sBgWorkspaceItems is passed to bindItems, which expects a list of all
    // folders and shortcuts created by LauncherModel that are directly on the
    // home screen (however, no widgets or shortcuts within folders).
    static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<ItemInfo>();

    // sBgDbIconCache is the set of ItemInfos that need to have their icons
    // updated in the database
    static final HashMap<Object, byte[]> sBgDbIconCache = new HashMap<Object, byte[]>();

    // </ only access in worker thread >

    private IconCache mIconCache;
    private Bitmap mDefaultIcon;

    protected int mPreviousConfigMcc;

    public interface Callbacks {
        public boolean setLoadOnResume();

        public void startBinding();

        public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end,
                boolean forceAnimateIcons);

        public void finishBindingItems(boolean upgradePath);

        public void bindAllApplications(ArrayList<AppInfo> apps);

        public void bindAppsAdded(ArrayList<AppInfo> addedApps);

        public void bindAppsUpdated(ArrayList<AppInfo> apps);

        public void bindComponentsRemoved(ArrayList<String> packageNames,
                ArrayList<AppInfo> appInfos,
                boolean matchPackageNamesOnly);

        public void bindPackagesUpdated(ArrayList<Object> widgetsAndShortcuts);

        public void bindSearchablesChanged();

        public boolean isAllAppsButtonRank(int rank);

        public void onPageBoundSynchronously(int page);

        public void dumpLogsToLocalData();
    }

    public interface ItemInfoFilter {
        public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn);
    }

    LauncherModel(LauncherAppState app, IconCache iconCache, AppFilter appFilter) {
        final Context context = app.getContext();

        mAppsCanBeOnRemoveableStorage = Environment.isExternalStorageRemovable();
        mApp = app;
        mBgAllAppsList = new AllAppsList(iconCache, appFilter);
        mIconCache = iconCache;

        mDefaultIcon = Utilities.createIconBitmap(
                mIconCache.getFullResDefaultActivityIcon(), context);

        final Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;
    }

    /**
     * Runs the specified runnable immediately if called from the main thread,
     * otherwise it is posted on the main thread handler.
     */
    private void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }

    private void runOnMainThread(Runnable r, int type) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r);
        } else {
            r.run();
        }
    }

    /**
     * Runs the specified runnable immediately if called from the worker thread,
     * otherwise it is posted on the worker thread handler.
     */
    private static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker
            // handler
            sWorker.post(r);
        }
    }

    static boolean findNextAvailableIconSpace(ArrayList<ItemInfo> items, int[] index) {
        return true;
    }

    public void addAndBindAddedApps(final Context context, final ArrayList<AppInfo> allAddedApps) {
        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
        addAndBindAddedApps(context, cb, allAddedApps);
    }

    public void addAndBindAddedApps(final Context context, final Callbacks callbacks,
            final ArrayList<AppInfo> allAddedApps) {
        if (allAddedApps.isEmpty()) {
            return;
        }
        // Process the newly added applications and add them to the database
        // first
        runOnMainThread(new Runnable() {
            public void run() {
                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                if (callbacks == cb && cb != null) {
                    callbacks.bindAppsAdded(allAddedApps);
                }
            }
        });
    }

    public Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }

    public void unbindItemInfosAndClearQueuedBindRunnables() {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            throw new RuntimeException("Expected unbindLauncherItemInfos() to be called from the " +
                    "main thread");
        }

        // Clear any deferred bind runnables
        mDeferredBindRunnables.clear();
        // Remove any queued bind runnables
        mHandler.cancelAllRunnablesOfType(MAIN_THREAD_BINDING_RUNNABLE);
        // Unbind all the workspace items
        unbindWorkspaceItemsOnMainThread();
    }

    /** Unbinds all the sBgWorkspaceItems and sBgAppWidgets on the main thread */
    void unbindWorkspaceItemsOnMainThread() {
        // Ensure that we don't use the same workspace items data structure on
        // the main thread by making a copy of workspace items first.
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();

        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (ItemInfo item : tmpWorkspaceItems) {
                    item.unbind();
                }
            }
        };
        runOnMainThread(r);
    }

    static void checkItemInfoLocked(
            final long itemId, final ItemInfo item, StackTraceElement[] stackTrace) {
        ItemInfo modelItem = sBgItemsIdMap.get(itemId);
        if (modelItem != null && item != modelItem) {
            // check all the data is consistent
            if (modelItem instanceof ShortcutInfo && item instanceof ShortcutInfo) {
                ShortcutInfo modelShortcut = (ShortcutInfo) modelItem;
                ShortcutInfo shortcut = (ShortcutInfo) item;
                if (modelShortcut.title.toString().equals(shortcut.title.toString()) &&
                        modelShortcut.intent.filterEquals(shortcut.intent) &&
                        modelShortcut.id == shortcut.id &&
                        modelShortcut.itemType == shortcut.itemType &&
                        modelShortcut.container == shortcut.container &&
                        modelShortcut.index == shortcut.index) {
                    // For all intents and purposes, this is the same object
                    return;
                }
            }

            // the modelItem needs to match up perfectly with item if our model
            // is
            // to be consistent with the database-- for now, just require
            // modelItem == item or the equality check above
            String msg = "item: " + ((item != null) ? item.toString() : "null") +
                    "modelItem: " +
                    ((modelItem != null) ? modelItem.toString() : "null") +
                    "Error: ItemInfo passed to checkItemInfo doesn't match original";
            RuntimeException e = new RuntimeException(msg);
            if (stackTrace != null) {
                e.setStackTrace(stackTrace);
            }
            // TODO: something breaks this in the upgrade path
            // throw e;
        }
    }

    public static void checkItemInfo(final ItemInfo item) {
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        final long itemId = item.id;
        Runnable r = new Runnable() {
            public void run() {
                synchronized (sBgLock) {
                    checkItemInfoLocked(itemId, item, stackTrace);
                }
            }
        };
        runOnWorkerThread(r);
    }

    public static void updateItemInDatabaseHelper(Context context, final ContentValues values,
            final ItemInfo item, final String callingFunction) {
        final long itemId = item.id;
        final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                cr.update(uri, values, null, null);
                updateItemArrays(item, itemId, stackTrace);
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemsInDatabaseHelper(Context context,
            final ArrayList<ContentValues> valuesList,
            final ArrayList<ItemInfo> items, final String callingFunction) {
        final ContentResolver cr = context.getContentResolver();

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                ArrayList<ContentProviderOperation> ops =
                        new ArrayList<ContentProviderOperation>();
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    ItemInfo item = items.get(i);
                    final long itemId = item.id;
                    final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
                    ContentValues values = valuesList.get(i);

                    ops.add(ContentProviderOperation.newUpdate(uri).withValues(values).build());
                    updateItemArrays(item, itemId, stackTrace);

                }
                try {
                    cr.applyBatch(LauncherProvider.AUTHORITY, ops);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemArrays(ItemInfo item, long itemId, StackTraceElement[] stackTrace) {
        // Lock on mBgLock *after* the db operation
        synchronized (sBgLock) {
            checkItemInfoLocked(itemId, item, stackTrace);

            // Items are added/removed from the corresponding FolderInfo
            // elsewhere, such as in Workspace.onDrop. Here, we just add/remove
            // them from the list of items that are on the desktop, as
            // appropriate
            ItemInfo modelItem = sBgItemsIdMap.get(itemId);
            if (modelItem.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                switch (modelItem.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        if (!sBgWorkspaceItems.contains(modelItem)) {
                            sBgWorkspaceItems.add(modelItem);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                sBgWorkspaceItems.remove(modelItem);
            }
        }
    }

    public void flushWorkerThread() {
        mFlushingWorkerThread = true;
        Runnable waiter = new Runnable() {
            public void run() {
                synchronized (this) {
                    notifyAll();
                    mFlushingWorkerThread = false;
                }
            }
        };

        synchronized (waiter) {
            runOnWorkerThread(waiter);
            if (mLoaderTask != null) {
                synchronized (mLoaderTask) {
                    mLoaderTask.notify();
                }
            }
            boolean success = false;
            while (!success) {
                try {
                    waiter.wait();
                    success = true;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    static void addAndBindAddedApps(Context context) {
    }

    /**
     * Move and/or resize item in the DB to a new <container, screen, cellX,
     * cellY, spanX, spanY>
     */
    static void modifyItemInDatabase(Context context, final ItemInfo item, final long container,
            int index) {
        item.container = container;
        item.index = index;

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.INDEX, item.index);

        updateItemInDatabaseHelper(context, values, item, "modifyItemInDatabase");
    }

    /**
     * Update an item to the database in a specified container.
     */
    static void updateItemInDatabase(Context context, final ItemInfo item) {
        final ContentValues values = new ContentValues();
        item.onAddToDatabase(values);
        item.updateValuesWithIndex(values, item.index);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }

    /**
     * Returns true if the shortcuts already exists in the database. we identify
     * a shortcut by its title and intent.
     */
    public static boolean shortcutExists(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] {
                        "title", "intent"
                }, "title=? and intent=?",
                new String[] {
                        title, intent.toUri(0)
                }, null);
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
        return result;
    }

    /**
     * Returns an ItemInfo array containing all the items in the LauncherModel.
     * The ItemInfo.id is not set through this function.
     */
    static ArrayList<ItemInfo> getItemsInLocalCoordinates(Context context) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
                LauncherSettings.Favorites.ITEM_TYPE, LauncherSettings.Favorites.CONTAINER,
                LauncherSettings.Favorites.INDEX
        }, null, null, null);

        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
        final int index = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INDEX);

        try {
            while (c.moveToNext()) {
                ItemInfo item = new ItemInfo();
                item.container = c.getInt(containerIndex);
                item.itemType = c.getInt(itemTypeIndex);
                item.index = c.getInt(index);

                items.add(item);
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }

        return items;
    }

    /**
     * Find a folder in the db, creating the FolderInfo if necessary, and adding
     * it to folderList.
     */
    // FolderInfo getFolderById(Context context, HashMap<Long,FolderInfo>
    // folderList, long id) {
    // final ContentResolver cr = context.getContentResolver();
    // Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
    // "_id=? and (itemType=? or itemType=?)",
    // new String[] { String.valueOf(id),
    // String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_FOLDER)}, null);
    //
    // try {
    // if (c.moveToFirst()) {
    // final int itemTypeIndex =
    // c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
    // final int titleIndex =
    // c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
    // final int containerIndex =
    // c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
    // final int screenIndex =
    // c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
    // final int cellXIndex =
    // c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
    // final int cellYIndex =
    // c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
    //
    // FolderInfo folderInfo = null;
    // switch (c.getInt(itemTypeIndex)) {
    // case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
    // folderInfo = findOrMakeFolder(folderList, id);
    // break;
    // }
    //
    // folderInfo.title = c.getString(titleIndex);
    // folderInfo.id = id;
    // folderInfo.container = c.getInt(containerIndex);
    // folderInfo.screenId = c.getInt(screenIndex);
    // folderInfo.cellX = c.getInt(cellXIndex);
    // folderInfo.cellY = c.getInt(cellYIndex);
    //
    // return folderInfo;
    // }
    // } finally {
    // c.close();
    // }
    //
    // return null;
    // }

    /**
     * Add an item to the database in a specified container. Sets the container,
     * screen, cellX and cellY fields of the item. Also assigns an ID to the
     * item.
     */
    static void addItemToDatabase(Context context, final ItemInfo item, final long container,
            final int index, final boolean notify) {
        item.container = container;
        item.index = index;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        item.onAddToDatabase(values);

        item.id = LauncherAppState.getLauncherProvider().generateNewItemId();
        values.put(LauncherSettings.Favorites._ID, item.id);
        item.updateValuesWithIndex(values, item.index);

        Runnable r = new Runnable() {
            public void run() {
                cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
                        LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    checkItemInfoLocked(item.id, item, null);
                    sBgItemsIdMap.put(item.id, item);
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                sBgWorkspaceItems.add(item);
                            }
                            break;
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Creates a new unique child id, for a given cell span across all layouts.
     */
    static int getCellLayoutChildId(
            long container, long screen, int localCellX, int localCellY, int spanX, int spanY) {
        return (((int) container & 0xFF) << 24)
                | ((int) screen & 0xFF) << 16 | (localCellX & 0xFF) << 8 | (localCellY & 0xFF);
    }

    /**
     * Removes the specified item from the database
     * 
     * @param context
     * @param item
     */
    static void deleteItemFromDatabase(Context context, final ItemInfo item) {
        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(item.id, false);

        Runnable r = new Runnable() {
            public void run() {
                cr.delete(uriToDelete, null, null);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            sBgWorkspaceItems.remove(item);
                            break;
                    }
                    sBgItemsIdMap.remove(item.id);
                    sBgDbIconCache.remove(item);
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED
     * and ACTION_PACKAGE_CHANGED.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG_LOADERS)
            Log.d(TAG, "onReceive intent=" + intent);

        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will
                // be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }
            }

            if (op != PackageUpdatedTask.OP_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] {
                        packageName
                }));
            }
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            // First, schedule to add these apps back in.
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages));
            // Then, rebind everything.
            startLoaderFromBackground();
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(
                    PackageUpdatedTask.OP_UNAVAILABLE, packages));
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // If we have changed locale we need to clear out the labels in all
            // apps/workspace.
            forceReload();
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
            // Check if configuration change was an mcc/mnc change which would
            // affect app resources and we would need to clear out the labels in
            // all apps/workspace. Same handling as above for
            // ACTION_LOCALE_CHANGED
            Configuration currentConfig = context.getResources().getConfiguration();
            if (mPreviousConfigMcc != currentConfig.mcc) {
                Log.d(TAG, "Reload apps on config change. curr_mcc:"
                        + currentConfig.mcc + " prevmcc:" + mPreviousConfigMcc);
                forceReload();
            }
            // Update previousConfig
            mPreviousConfigMcc = currentConfig.mcc;
        } else if (SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED.equals(action) ||
                SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals(action)) {
            if (mCallbacks != null) {
                Callbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    callbacks.bindSearchablesChanged();
                }
            }
        }
    }

    private void forceReload() {
        resetLoadedState(true, true);

        // Do this here because if the launcher activity is running it will be
        // restarted.
        // If it's not running startLoaderFromBackground will merely tell it
        // that it needs to reload.
        startLoaderFromBackground();
    }

    public void resetLoadedState(boolean resetAllAppsLoaded, boolean resetWorkspaceLoaded) {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded
            // or mWorkspaceLoaded to true later
            stopLoaderLocked();
            if (resetAllAppsLoaded)
                mAllAppsLoaded = false;
            if (resetWorkspaceLoaded)
                mWorkspaceLoaded = false;
        }
    }

    /**
     * When the launcher is in the background, it's possible for it to miss
     * paired configuration changes. So whenever we trigger the loader from the
     * background tell the launcher that it needs to re-run the loader when it
     * comes back instead of doing it now.
     */
    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                if (!callbacks.setLoadOnResume()) {
                    runLoader = true;
                }
            }
        }
        if (runLoader) {
            startLoader(false);
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public void startLoader(boolean isLaunching) {
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "startLoader isLaunching=" + isLaunching);
            }

            // Clear any deferred bind-runnables from the synchronized load
            // process We must do this before any loading/binding is scheduled
            // below.
            mDeferredBindRunnables.clear();

            // Don't bother to start the thread if we know it's not going to do
            // anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop. also, don't
                // downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(mApp.getContext(), isLaunching);
                if (mAllAppsLoaded && mWorkspaceLoaded) {
                    mLoaderTask.runBindWorkspace();
                } else {
                    sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                    sWorker.post(mLoaderTask);
                }
            }
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }

    public boolean isAllAppsLoaded() {
        return mAllAppsLoaded;
    }

    boolean isLoadingWorkspace() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                return mLoaderTask.isLoadingWorkspace();
            }
        }
        return false;
    }

    /**
     * Runnable for the thread that loads the contents of the launcher: -
     * workspace icons - widgets - all apps icons
     */
    private class LoaderTask implements Runnable {
        private Context mContext;
        private boolean mIsLaunching;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;

        private HashMap<Object, CharSequence> mLabelCache;

        LoaderTask(Context context, boolean isLaunching) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        boolean isLoadingWorkspace() {
            return mIsLoadingAndBindingWorkspace;
        }

        /** Returns whether this is an upgrade path */
        private boolean loadAndBindWorkspace() {
            mIsLoadingAndBindingWorkspace = true;

            // Load the workspace
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindWorkspace mWorkspaceLoaded=" + mWorkspaceLoaded);
            }

            boolean isUpgradePath = false;
            if (!mWorkspaceLoaded) {
                isUpgradePath = loadWorkspace();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return isUpgradePath;
                    }
                    mWorkspaceLoaded = true;
                }
            }

            // Bind the workspace
            bindWorkspace(isUpgradePath);
            return isUpgradePath;
        }

        private void waitForIdle() {
            // Wait until the either we're stopped or the other threads are
            // done.
            // This way we don't start loading all apps until the workspace has
            // settled
            // down.
            synchronized (LoaderTask.this) {
                final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                mHandler.postIdle(new Runnable() {
                    public void run() {
                        synchronized (LoaderTask.this) {
                            mLoadAndBindStepFinished = true;
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "done with previous binding step");
                            }
                            LoaderTask.this.notify();
                        }
                    }
                });

                while (!mStopped && !mLoadAndBindStepFinished && !mFlushingWorkerThread) {
                    try {
                        // Just in case mFlushingWorkerThread changes but we
                        // aren't woken up,
                        // wait no longer than 1sec at a time
                        this.wait(1000);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "waited "
                            + (SystemClock.uptimeMillis() - workspaceWaitTime)
                            + "ms for previous step to finish binding");
                }
            }
        }

        void runBindWorkspace() {
            if (!mAllAppsLoaded || !mWorkspaceLoaded) {
                // Ensure that we don't try and bind a specified page when the
                // pages have not been loaded already (we should load everything
                // asynchronously in that case)
                throw new RuntimeException("Expecting AllApps and Workspace to be loaded");
            }

            synchronized (mLock) {
                if (mIsLoaderTaskRunning) {
                    // Ensure that we are never running the background loading
                    // at this point since we also touch the background
                    // collections
                    throw new RuntimeException("Error! Background loading is already running");
                }
            }

            // XXX: Throw an exception if we are already loading (since we touch
            // the worker thread data structures, we can't allow any other
            // thread to touch that data, but because this call is synchronous,
            // we can get away with not locking).

            // The LauncherModel is static in the LauncherAppState and mHandler
            // may have queued operations from the previous activity. We need to
            // ensure that all queued operations are executed before any
            // synchronous binding work is done.
            mHandler.flush();

            // Divide the set of loaded items into those that we are binding
            // synchronously, and everything else that is to be bound normally
            // (asynchronously).
            bindWorkspace(false);

            // XXX: For now, continue posting the binding of AllApps as there
            // are other issues that arise from that.
            onlyBindAllApps();
        }

        public void run() {
            boolean isUpgrade = false;

            synchronized (mLock) {
                mIsLoaderTaskRunning = true;
            }
            // Optimize for end-user experience: if the Launcher is up and //
            // running with the
            // All Apps interface in the foreground, load All Apps first.
            // Otherwise, load the
            // workspace first (default).
            keep_running: {
                // Elevate priority when Home launches for the first time to
                // avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    if (DEBUG_LOADERS)
                        Log.d(TAG, "Setting thread priority to " +
                                (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }
                if (DEBUG_LOADERS)
                    Log.d(TAG, "step 1: loading workspace");
                isUpgrade = loadAndBindWorkspace();

                if (mStopped) {
                    break keep_running;
                }

                // Whew! Hard work done. Slow us down, and wait until the UI
                // thread has
                // settled down.
                synchronized (mLock) {
                    if (mIsLaunching) {
                        if (DEBUG_LOADERS)
                            Log.d(TAG, "Setting thread priority to BACKGROUND");
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                waitForIdle();

                // second step
                if (DEBUG_LOADERS)
                    Log.d(TAG, "step 2: loading all apps");
                loadAndBindAllApps();

                // Restore the default thread priority after we are done loading
                // items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }

            // Update the saved icons if necessary
            if (DEBUG_LOADERS)
                Log.d(TAG, "Comparing loaded icons to database icons");
            synchronized (sBgLock) {
                for (Object key : sBgDbIconCache.keySet()) {
                    updateSavedIcon(mContext, (ShortcutInfo) key, sBgDbIconCache.get(key));
                }
                sBgDbIconCache.clear();
            }

            verifyApplications();

            // Clear out this reference, otherwise we end up holding it until
            // all of the callback runnables are done.
            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove
                // ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
            }
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        /**
         * Gets the callbacks object. If we've been stopped, or if the launcher
         * object has somehow been garbage collected, return null instead. Pass
         * in the Callbacks object that was around when the deferred message was
         * scheduled, and if there's a new Callbacks object around then also
         * return null. This will save us from calling onto it with data that
         * will be ignored.
         */
        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    Log.w(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        private void verifyApplications() {
            final Context context = mApp.getContext();

            // Cross reference all the applications in our apps list with items
            // in the workspace
            ArrayList<ItemInfo> tmpInfos;
            ArrayList<AppInfo> added = new ArrayList<AppInfo>();
            synchronized (sBgLock) {
                for (AppInfo app : mBgAllAppsList.data) {
                    tmpInfos = getItemInfoForComponentName(app.componentName);
                    if (tmpInfos.isEmpty()) {
                        // We are missing an application icon, so add this to
                        // the workspace
                        added.add(app);
                        // This is a rare event, so lets log it
                        Log.e(TAG, "Missing Application on load: " + app);
                    }
                }
            }
            if (!added.isEmpty()) {
                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                addAndBindAddedApps(context, cb, added);
            }
        }

        /**
         * Return true if the item is out of bounds.
         */
        private boolean checkItemDimensions(ItemInfo info) {
            return false;
        }

        /**
         * Check & update map of what's occupied; used to discard
         * overlapping/invalid items. Return true if passed.
         */
        private boolean checkItemPlacement(HashMap<Long, ItemInfo[][]> occupied, ItemInfo item,
                AtomicBoolean deleteOnItemOverlap) {
            return true;
        }

        /** Clears all the sBg data structures */
        private void clearSBgDataStructures() {
            synchronized (sBgLock) {
                sBgWorkspaceItems.clear();
                sBgItemsIdMap.clear();
                sBgDbIconCache.clear();
            }
        }

        /** Returns whether this is an upgrade path */
        private boolean loadWorkspace() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();

            // Make sure the default workspace is loaded, if needed
            LauncherAppState.getLauncherProvider().loadDefaultFavoritesIfNecessary(0);

            // Check if we need to do any upgrade-path logic
            boolean loadedOldDb = LauncherAppState.getLauncherProvider().justLoadedOldDb();

            synchronized (sBgLock) {
                clearSBgDataStructures();

                final ArrayList<Long> itemsToRemove = new ArrayList<Long>();
                final Uri contentUri = LauncherSettings.Favorites.CONTENT_URI;
                if (DEBUG_LOADERS)
                    Log.d(TAG, "loading model from " + contentUri);
                final Cursor c = contentResolver.query(contentUri, null, null, null, null);

                // +1 for the hotseat (it can be larger than the workspace)
                // Load workspace in reverse order to ensure that latest items
                // are loaded first (and
                // before any earlier duplicates)
                final HashMap<Long, ItemInfo[][]> occupied = new HashMap<Long, ItemInfo[][]>();

                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.INTENT);
                    final int titleIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE);
                    final int iconTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_TYPE);
                    final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
                    final int iconPackageIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_PACKAGE);
                    final int iconResourceIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_RESOURCE);
                    final int containerIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CONTAINER);
                    final int itemTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ITEM_TYPE);
                    final int index = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.INDEX);
                    final int uriIndex =
                            c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);

                    ShortcutInfo info;
                    String intentDescription;
                    int container;
                    long id;
                    Intent intent;

                    while (!mStopped && c.moveToNext()) {
                        AtomicBoolean deleteOnItemOverlap = new AtomicBoolean(false);
                        try {
                            int itemType = c.getInt(itemTypeIndex);

                            switch (itemType) {
                                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                                    id = c.getLong(idIndex);
                                    intentDescription = c.getString(intentIndex);
                                    try {
                                        intent = Intent.parseUri(intentDescription, 0);
                                        ComponentName cn = intent.getComponent();
                                        if (cn != null && !isValidPackageComponent(manager, cn)) {
                                            if (!mAppsCanBeOnRemoveableStorage) {
                                                // Log the invalid package, and
                                                // remove it from the db
                                                Logger.addDumpLog(TAG,
                                                        "Invalid package removed: " + cn, true);
                                                itemsToRemove.add(id);
                                            } else {
                                                // If apps can be on external
                                                // storage, then we just
                                                // leave them for the user to
                                                // remove (maybe add
                                                // visual treatment to it)
                                                Logger.addDumpLog(TAG,
                                                        "Invalid package found: " + cn, true);
                                            }
                                            continue;
                                        }
                                    } catch (URISyntaxException e) {
                                        Logger.addDumpLog(TAG, "Invalid uri: "
                                                + intentDescription, true);
                                        continue;
                                    }

                                    if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                                        info = getShortcutInfo(manager, intent, context, c,
                                                iconIndex,
                                                titleIndex, mLabelCache);
                                    } else {
                                        info = getShortcutInfo(c, context, iconTypeIndex,
                                                iconPackageIndex, iconResourceIndex, iconIndex,
                                                titleIndex);

                                        // App shortcuts that used to be
                                        // automatically added to Launcher
                                        // didn't always have the correct intent
                                        // flags set, so do that
                                        // here
                                        if (intent.getAction() != null
                                                &&
                                                intent.getCategories() != null
                                                &&
                                                intent.getAction().equals(Intent.ACTION_MAIN)
                                                &&
                                                intent.getCategories().contains(
                                                        Intent.CATEGORY_LAUNCHER)) {
                                            intent.addFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                                            |
                                                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                        }
                                    }

                                    if (info != null) {
                                        info.id = id;
                                        info.intent = intent;
                                        container = c.getInt(containerIndex);
                                        info.container = container;
                                        info.index = index;
                                        // Skip loading items that are out of
                                        // bounds
                                        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                            if (checkItemDimensions(info)) {
                                                Logger.addDumpLog(TAG,
                                                        "Skipped loading out of bounds shortcut: "
                                                                + info + ", " + 4/*
                                                                                  * grid
                                                                                  * .
                                                                                  * numColumns
                                                                                  */+ "x" + 4/*
                                                                                              * grid
                                                                                              * .
                                                                                              * numRows
                                                                                              */,
                                                        true);
                                                continue;
                                            }
                                        }
                                        // check & update map of what's occupied
                                        deleteOnItemOverlap.set(false);
                                        if (!checkItemPlacement(occupied, info, deleteOnItemOverlap)) {
                                            if (deleteOnItemOverlap.get()) {
                                                itemsToRemove.add(id);
                                            }
                                            break;
                                        }

                                        switch (container) {
                                            case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                                sBgWorkspaceItems.add(info);
                                                break;
                                            default:
                                                break;
                                        }
                                        sBgItemsIdMap.put(info.id, info);

                                        // now that we've loaded everything
                                        // re-save it with the icon in case it
                                        // disappears somehow.
                                        queueIconToBeChecked(sBgDbIconCache, info, c, iconIndex);
                                    } else {
                                        throw new RuntimeException("Unexpected null ShortcutInfo");
                                    }
                                    break;
                            }
                        } catch (Exception e) {
                            Logger.addDumpLog(TAG,
                                    "Desktop items loading interrupted: " + e, true);
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                // Break early if we've stopped loading
                if (mStopped) {
                    clearSBgDataStructures();
                    return false;
                }

                if (itemsToRemove.size() > 0) {
                    ContentProviderClient client = contentResolver.acquireContentProviderClient(
                            LauncherSettings.Favorites.CONTENT_URI);
                    // Remove dead items
                    for (long id : itemsToRemove) {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "Removed id = " + id);
                        }
                        // Don't notify content observers
                        try {
                            client.delete(LauncherSettings.Favorites.getContentUri(id, false),
                                    null, null);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Could not remove id = " + id);
                        }
                    }
                }

                if (loadedOldDb) {
                    // Update the max item id after we load an old db
                    long maxItemId = 0;
                    // If we're importing we use the old screen order.
                    for (ItemInfo item : sBgItemsIdMap.values()) {
                        maxItemId = Math.max(maxItemId, item.id);
                    }
                    LauncherAppState.getLauncherProvider().updateMaxItemId(maxItemId);
                }

                if (DEBUG_LOADERS) {
                    Logger.debug(TAG, "loaded workspace in " + (SystemClock.uptimeMillis() - t)
                            + "ms");
                    Logger.debug(TAG, "workspace layout: ");
                }
            }
            return loadedOldDb;
        }

        /**
         * Sorts the set of items by workspace (spatially from top to bottom,
         * left to right)
         */
        private void sortWorkspaceItemsSpatially(ArrayList<ItemInfo> workspaceItems) {
            // Sort items by index.
            Collections.sort(workspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    return (int) (lhs.index - rhs.index);
                }
            });
        }

        private void bindWorkspaceItems(final Callbacks oldCallbacks,
                final ArrayList<ItemInfo> workspaceItems,
                ArrayList<Runnable> deferredBindRunnables) {
            final boolean postOnMainThread = (deferredBindRunnables != null);

            // Bind the workspace items
            int N = workspaceItems.size();
            for (int i = 0; i < N; i += ITEMS_CHUNK) {
                final int start = i;
                final int chunkSize = (i + ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N - i);
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindItems(workspaceItems, start, start + chunkSize,
                                    false);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }
        }

        /**
         * Binds all loaded data to actual views on the main thread.
         */
        private void bindWorkspace(final boolean isUpgradePath) {
            final long t = SystemClock.uptimeMillis();
            Runnable r;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us. Just
                // bail.
                Log.w(TAG, "LoaderTask running with no launcher");
                return;
            }

            // Load all the items that are on the current page first (and in the
            // process, unbind all the existing workspace items before we call
            // startBinding() below.
            unbindWorkspaceItemsOnMainThread();

            ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
            HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();

            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
                itemsIdMap.putAll(sBgItemsIdMap);
            }

            sortWorkspaceItemsSpatially(workspaceItems);

            // Tell the workspace that we're about to start binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding();
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);

            // Load items on workspace
            bindWorkspaceItems(oldCallbacks, workspaceItems, null);

            // Tell the workspace that we're done binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems(isUpgradePath);
                    }

                    // If we're profiling, ensure this is the last thing in the
                    // queue.
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound workspace in "
                                + (SystemClock.uptimeMillis() - t) + "ms");
                    }

                    mIsLoadingAndBindingWorkspace = false;
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
        }

        private void loadAndBindAllApps() {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindAllApps mAllAppsLoaded=" + mAllAppsLoaded);
            }
            if (!mAllAppsLoaded) {
                loadAllApps();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }

        private void onlyBindAllApps() {
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us. Just
                // bail.
                Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }

            // shallow copy
            @SuppressWarnings("unchecked")
            final ArrayList<AppInfo> list = (ArrayList<AppInfo>) mBgAllAppsList.data.clone();
            Runnable r = new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(list);
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound all " + list.size() + " apps from cache in "
                                + (SystemClock.uptimeMillis() - t) + "ms");
                    }
                }
            };
            boolean isRunningOnMainThread = !(sWorkerThread.getThreadId() == Process.myTid());
            if (isRunningOnMainThread) {
                r.run();
            } else {
                mHandler.post(r);
            }
        }

        private void loadAllApps() {
            final long loadTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us. Just
                // bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAllApps)");
                return;
            }

            final PackageManager packageManager = mContext.getPackageManager();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            // Clear the list of apps
            mBgAllAppsList.clear();

            // Query for the set of apps
            final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
            List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
            if (DEBUG_LOADERS) {
                Log.d(TAG, "queryIntentActivities took "
                        + (SystemClock.uptimeMillis() - qiaTime) + "ms");
                Log.d(TAG, "queryIntentActivities got " + apps.size() + " apps");
            }
            // Fail if we don't have any apps
            if (apps == null || apps.isEmpty()) {
                return;
            }
            // Sort the applications by name
            final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
            Collections.sort(apps,
                    new LauncherModel.ShortcutNameComparator(packageManager, mLabelCache));
            if (DEBUG_LOADERS) {
                Log.d(TAG, "sort took "
                        + (SystemClock.uptimeMillis() - sortTime) + "ms");
            }

            // Create the ApplicationInfos
            for (int i = 0; i < apps.size(); i++) {
                ResolveInfo app = apps.get(i);
                // This builds the icon bitmaps.
                mBgAllAppsList.add(new AppInfo(packageManager, app,
                        mIconCache, mLabelCache));
            }

            // Huh? Shouldn't this be inside the Runnable below?
            final ArrayList<AppInfo> added = mBgAllAppsList.added;
            mBgAllAppsList.added = new ArrayList<AppInfo>();

            // Post callback on main thread
            mHandler.post(new Runnable() {
                public void run() {
                    final long bindTime = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(added);
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "bound " + added.size() + " apps in "
                                    + (SystemClock.uptimeMillis() - bindTime) + "ms");
                        }
                    } else {
                        Log.i(TAG, "not binding apps: no Launcher activity");
                    }
                }
            });

            if (DEBUG_LOADERS) {
                Log.d(TAG, "Icons processed in "
                        + (SystemClock.uptimeMillis() - loadTime) + "ms");
            }
        }

        public void dumpState() {
            synchronized (sBgLock) {
                Log.d(TAG, "mLoaderTask.mContext=" + mContext);
                Log.d(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
                Log.d(TAG, "mLoaderTask.mStopped=" + mStopped);
                Log.d(TAG, "mLoaderTask.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
                Log.d(TAG, "mItems size=" + sBgWorkspaceItems.size());
            }
        }
    }

    void enqueuePackageUpdated(PackageUpdatedTask task) {
        sWorker.post(task);
    }

    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted

        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final Context context = mApp.getContext();

            final String[] packages = mPackages;
            final int N = packages.length;
            switch (mOp) {
                case OP_ADD:
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS)
                            Log.d(TAG, "mAllAppsList.addPackage " + packages[i]);
                        mBgAllAppsList.addPackage(context, packages[i]);
                    }
                    break;
                case OP_UPDATE:
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS)
                            Log.d(TAG, "mAllAppsList.updatePackage " + packages[i]);
                        mBgAllAppsList.updatePackage(context, packages[i]);
                        // WidgetPreviewLoader.removePackageFromDb(
                        // mApp.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
                case OP_REMOVE:
                case OP_UNAVAILABLE:
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS)
                            Log.d(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mBgAllAppsList.removePackage(packages[i]);
                        // WidgetPreviewLoader.removePackageFromDb(
                        // mApp.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
            }

            ArrayList<AppInfo> added = null;
            ArrayList<AppInfo> modified = null;
            final ArrayList<AppInfo> removedApps = new ArrayList<AppInfo>();

            if (mBgAllAppsList.added.size() > 0) {
                added = new ArrayList<AppInfo>(mBgAllAppsList.added);
                mBgAllAppsList.added.clear();
            }
            if (mBgAllAppsList.modified.size() > 0) {
                modified = new ArrayList<AppInfo>(mBgAllAppsList.modified);
                mBgAllAppsList.modified.clear();
            }
            if (mBgAllAppsList.removed.size() > 0) {
                removedApps.addAll(mBgAllAppsList.removed);
                mBgAllAppsList.removed.clear();
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }

            if (added != null) {
                // Ensure that we add all the workspace applications to the db
                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                addAndBindAddedApps(context, cb, added);
            }
            if (modified != null) {
                final ArrayList<AppInfo> modifiedFinal = modified;

                // Update the launcher db to reflect the changes
                for (AppInfo a : modifiedFinal) {
                    ArrayList<ItemInfo> infos =
                            getItemInfoForComponentName(a.componentName);
                    for (ItemInfo i : infos) {
                        if (isShortcutInfoUpdateable(i)) {
                            ShortcutInfo info = (ShortcutInfo) i;
                            info.title = a.title.toString();
                            updateItemInDatabase(context, info);
                        }
                    }
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsUpdated(modifiedFinal);
                        }
                    }
                });
            }
            // If a package has been removed, or an app has been removed as a
            // result of
            // an update (for example), make the removed callback.
            if (mOp == OP_REMOVE || !removedApps.isEmpty()) {
                final boolean packageRemoved = (mOp == OP_REMOVE);
                final ArrayList<String> removedPackageNames =
                        new ArrayList<String>(Arrays.asList(packages));

                // Update the launcher db to reflect the removal of apps
                if (packageRemoved) {
                    for (String pn : removedPackageNames) {
                        ArrayList<ItemInfo> infos = getItemInfoForPackageName(pn);
                        for (ItemInfo i : infos) {
                            deleteItemFromDatabase(context, i);
                        }
                    }

                    // Remove any queued items from the install queue
                    String spKey = LauncherAppState.getSharedPreferencesKey();
                    SharedPreferences sp =
                            context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
                    InstallShortcutReceiver.removeFromInstallQueue(sp, removedPackageNames);
                } else {
                    for (AppInfo a : removedApps) {
                        ArrayList<ItemInfo> infos =
                                getItemInfoForComponentName(a.componentName);
                        for (ItemInfo i : infos) {
                            deleteItemFromDatabase(context, i);
                        }
                    }
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindComponentsRemoved(removedPackageNames,
                                    removedApps, packageRemoved);
                        }
                    }
                });
            }

            final ArrayList<Object> widgetsAndShortcuts =
                    getSortedWidgetsAndShortcuts(context);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.bindPackagesUpdated(widgetsAndShortcuts);
                    }
                }
            });

            // Write all the logs to disk
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.dumpLogsToLocalData();
                    }
                }
            });
        }
    }

    // Returns a list of ResolveInfos/AppWindowInfos in sorted order
    public static ArrayList<Object> getSortedWidgetsAndShortcuts(Context context) {
        PackageManager packageManager = context.getPackageManager();
        final ArrayList<Object> widgetsAndShortcuts = new ArrayList<Object>();
        widgetsAndShortcuts.addAll(AppWidgetManager.getInstance(context).getInstalledProviders());
        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        widgetsAndShortcuts.addAll(packageManager.queryIntentActivities(shortcutsIntent, 0));
        Collections.sort(widgetsAndShortcuts,
                new LauncherModel.WidgetAndShortcutNameComparator(packageManager));
        return widgetsAndShortcuts;
    }

    private boolean isValidPackageComponent(PackageManager pm, ComponentName cn) {
        if (cn == null) {
            return false;
        }

        try {
            // Skip if the application is disabled
            PackageInfo pi = pm.getPackageInfo(cn.getPackageName(), 0);
            if (!pi.applicationInfo.enabled) {
                return false;
            }

            // Check the activity
            return (pm.getActivityInfo(cn, 0) != null);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    /**
     * This is called from the code that adds shortcuts from the intent
     * receiver. This doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        return getShortcutInfo(manager, intent, context, null, -1, -1, null);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application. If c
     * is not null, then it will be used to fill in missing data like the title
     * and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int iconIndex, int titleIndex, HashMap<Object, CharSequence> labelCache) {
        ComponentName componentName = intent.getComponent();
        final ShortcutInfo info = new ShortcutInfo();
        if (componentName != null && !isValidPackageComponent(manager, componentName)) {
            Log.d(TAG, "Invalid package found in getShortcutInfo: " + componentName);
            return null;
        } else {
            try {
                PackageInfo pi = manager.getPackageInfo(componentName.getPackageName(), 0);
                info.initFlagsAndFirstInstallTime(pi);
            } catch (NameNotFoundException e) {
                Log.d(TAG, "getPackInfo failed for package " +
                        componentName.getPackageName());
            }
        }

        // TODO: See if the PackageManager knows about this case. If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that. All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.

        // Attempt to use queryIntentActivities to get the ResolveInfo (with
        // IntentFilter info) and
        // if that fails, or is ambiguious, fallback to the standard way of
        // getting the resolve info
        // via resolveActivity().
        Bitmap icon = null;
        ResolveInfo resolveInfo = null;
        ComponentName oldComponent = intent.getComponent();
        Intent newIntent = new Intent(intent.getAction(), null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setPackage(oldComponent.getPackageName());
        List<ResolveInfo> infos = manager.queryIntentActivities(newIntent, 0);
        for (ResolveInfo i : infos) {
            ComponentName cn = new ComponentName(i.activityInfo.packageName,
                    i.activityInfo.name);
            if (cn.equals(oldComponent)) {
                resolveInfo = i;
            }
        }
        if (resolveInfo == null) {
            resolveInfo = manager.resolveActivity(intent, 0);
        }
        if (resolveInfo != null) {
            icon = mIconCache.getIcon(componentName, resolveInfo, labelCache);
        }
        // the db
        if (icon == null) {
            if (c != null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
        }
        // the fallback icon
        if (icon == null) {
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        // from the resource
        if (resolveInfo != null) {
            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(resolveInfo);
            if (labelCache != null && labelCache.containsKey(key)) {
                info.title = labelCache.get(key);
            } else {
                info.title = resolveInfo.activityInfo.loadLabel(manager);
                if (labelCache != null) {
                    labelCache.put(key, info.title);
                }
            }
        }
        // from the db
        if (info.title == null) {
            if (c != null) {
                info.title = c.getString(titleIndex);
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }

    static ArrayList<ItemInfo> filterItemInfos(Collection<ItemInfo> infos,
            ItemInfoFilter f) {
        HashSet<ItemInfo> filtered = new HashSet<ItemInfo>();
        for (ItemInfo i : infos) {
            if (i instanceof ShortcutInfo) {
                ShortcutInfo info = (ShortcutInfo) i;
                ComponentName cn = info.intent.getComponent();
                if (cn != null && f.filterItem(null, info, cn)) {
                    filtered.add(info);
                }
            }
        }
        return new ArrayList<ItemInfo>(filtered);
    }

    private ArrayList<ItemInfo> getItemInfoForPackageName(final String pn) {
        ItemInfoFilter filter = new ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn) {
                return cn.getPackageName().equals(pn);
            }
        };
        return filterItemInfos(sBgItemsIdMap.values(), filter);
    }

    private ArrayList<ItemInfo> getItemInfoForComponentName(final ComponentName cname) {
        ItemInfoFilter filter = new ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn) {
                return cn.equals(cname);
            }
        };
        return filterItemInfos(sBgItemsIdMap.values(), filter);
    }

    public static boolean isShortcutInfoUpdateable(ItemInfo i) {
        if (i instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) i;
            // We need to check for ACTION_MAIN otherwise getComponent() might
            // return null for some shortcuts (for instance, for shortcuts to
            // web pages.)
            Intent intent = info.intent;
            ComponentName name = intent.getComponent();
            if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                    Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make an ShortcutInfo object for a shortcut that isn't an application.
     */
    private ShortcutInfo getShortcutInfo(Cursor c, Context context,
            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex,
            int titleIndex) {

        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;

        // TODO: If there's an explicit component and we can't install that,
        // delete it.

        info.title = c.getString(titleIndex);

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
            case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
                String packageName = c.getString(iconPackageIndex);
                String resourceName = c.getString(iconResourceIndex);
                PackageManager packageManager = context.getPackageManager();
                info.customIcon = false;
                // the resource
                try {
                    Resources resources = packageManager.getResourcesForApplication(packageName);
                    if (resources != null) {
                        final int id = resources.getIdentifier(resourceName, null, null);
                        icon = Utilities.createIconBitmap(
                                mIconCache.getFullResIcon(resources, id), context);
                    }
                } catch (Exception e) {
                    // drop this. we have other places to look for icons
                }
                // the db
                if (icon == null) {
                    icon = getIconFromCursor(c, iconIndex, context);
                }
                // the fallback icon
                if (icon == null) {
                    icon = getFallbackIcon();
                    info.usingFallbackIcon = true;
                }
                break;
            case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
                icon = getIconFromCursor(c, iconIndex, context);
                if (icon == null) {
                    icon = getFallbackIcon();
                    info.customIcon = false;
                    info.usingFallbackIcon = true;
                } else {
                    info.customIcon = true;
                }
                break;
            default:
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
                info.customIcon = false;
                break;
        }
        info.setIcon(icon);
        return info;
    }

    Bitmap getIconFromCursor(Cursor c, int iconIndex, Context context) {
        @SuppressWarnings("all")
        // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            Log.d(TAG, "getIconFromCursor app="
                    + c.getString(c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE)));
        }
        byte[] data = c.getBlob(iconIndex);
        try {
            return Utilities.createIconBitmap(
                    BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    ShortcutInfo addShortcut(Context context, Intent data, long container, int index, boolean notify) {
        final ShortcutInfo info = infoFromShortcutIntent(context, data, null);
        if (info == null) {
            return null;
        }
        addItemToDatabase(context, info, container, index, notify);

        return info;
    }

    /**
     * Attempts to find an AppWidgetProviderInfo that matches the given
     * component.
     */
    AppWidgetProviderInfo findAppWidgetProviderInfoWithComponent(Context context,
            ComponentName component) {
        List<AppWidgetProviderInfo> widgets =
                AppWidgetManager.getInstance(context).getInstalledProviders();
        for (AppWidgetProviderInfo info : widgets) {
            if (info.provider.equals(component)) {
                return info;
            }
        }
        return null;
    }

    public ShortcutInfo infoFromShortcutIntent(Context context, Intent data, Bitmap fallbackIcon) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo,
            // so we return null
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }

        Bitmap icon = null;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = Utilities.createIconBitmap(new FastBitmapDrawable((Bitmap) bitmap), context);
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = Utilities.createIconBitmap(
                            mIconCache.getFullResIcon(resources, id), context);
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        if (icon == null) {
            if (fallbackIcon != null) {
                icon = fallbackIcon;
            } else {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
        }
        info.setIcon(icon);

        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;

        return info;
    }

    boolean queueIconToBeChecked(HashMap<Object, byte[]> cache, ShortcutInfo info, Cursor c,
            int iconIndex) {
        // If apps can't be on SD, don't even bother.
        if (!mAppsCanBeOnRemoveableStorage) {
            return false;
        }
        // If this icon doesn't have a custom icon, check to see
        // what's stored in the DB, and if it doesn't match what
        // we're going to show, store what we are going to show back
        // into the DB. We do this so when we're loading, if the
        // package manager can't find an icon (for example because
        // the app is on SD) then we can use that instead.
        if (!info.customIcon && !info.usingFallbackIcon) {
            cache.put(info, c.getBlob(iconIndex));
            return true;
        }
        return false;
    }

    @SuppressLint("NewApi")
    void updateSavedIcon(Context context, ShortcutInfo info, byte[] data) {
        boolean needSave = false;
        try {
            if (data != null) {
                Bitmap saved = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap loaded = info.getIcon(mIconCache);
                needSave = !saved.sameAs(loaded);
            } else {
                needSave = true;
            }
        } catch (Exception e) {
            needSave = true;
        }
        if (needSave) {
            Log.d(TAG, "going to save icon bitmap for info=" + info);
            // This is slower than is ideal, but this only happens once
            // or when the app is updated with a new icon.
            updateItemInDatabase(context, info);
        }
    }

    public static final Comparator<AppInfo> getAppNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppInfo>() {
            public final int compare(AppInfo a, AppInfo b) {
                int result = collator.compare(a.title.toString().trim(),
                        b.title.toString().trim());
                if (result == 0) {
                    result = a.componentName.compareTo(b.componentName);
                }
                return result;
            }
        };
    }

    public static final Comparator<AppInfo> APP_INSTALL_TIME_COMPARATOR = new Comparator<AppInfo>() {
        public final int compare(AppInfo a, AppInfo b) {
            if (a.firstInstallTime < b.firstInstallTime)
                return 1;
            if (a.firstInstallTime > b.firstInstallTime)
                return -1;
            return 0;
        }
    };

    public static final Comparator<AppWidgetProviderInfo> getWidgetNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppWidgetProviderInfo>() {
            public final int compare(AppWidgetProviderInfo a, AppWidgetProviderInfo b) {
                return collator.compare(a.label.toString().trim(), b.label.toString().trim());
            }
        };
    }

    public static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }

    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;

        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
            mCollator = Collator.getInstance();
        }

        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }

        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = LauncherModel.getComponentNameFromResolveInfo(a);
            ComponentName keyB = LauncherModel.getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString().trim();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString().trim();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };

    public static class WidgetAndShortcutNameComparator implements Comparator<Object> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, String> mLabelCache;

        WidgetAndShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, String>();
            mCollator = Collator.getInstance();
        }

        public final int compare(Object a, Object b) {
            String labelA, labelB;
            if (mLabelCache.containsKey(a)) {
                labelA = mLabelCache.get(a);
            } else {
                labelA = (a instanceof AppWidgetProviderInfo) ?
                        ((AppWidgetProviderInfo) a).label :
                        ((ResolveInfo) a).loadLabel(mPackageManager).toString().trim();
                mLabelCache.put(a, labelA);
            }
            if (mLabelCache.containsKey(b)) {
                labelB = mLabelCache.get(b);
            } else {
                labelB = (b instanceof AppWidgetProviderInfo) ?
                        ((AppWidgetProviderInfo) b).label :
                        ((ResolveInfo) b).loadLabel(mPackageManager).toString().trim();
                mLabelCache.put(b, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };

    public void dumpState() {
        Log.d(TAG, "mCallbacks=" + mCallbacks);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mBgAllAppsList.data);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mBgAllAppsList.added);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mBgAllAppsList.removed);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mBgAllAppsList.modified);
        if (mLoaderTask != null) {
            mLoaderTask.dumpState();
        } else {
            Log.d(TAG, "mLoaderTask=null");
        }
    }
}
