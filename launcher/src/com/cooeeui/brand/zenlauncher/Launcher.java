
package com.cooeeui.brand.zenlauncher;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.debug.Logger;
import com.cooeeui.brand.zenlauncher.scenes.LoadingView;
import com.cooeeui.brand.zenlauncher.scenes.Workspace;
import com.cooeeui.brand.zenlauncher.scenes.ZenSetting;
import com.cooeeui.brand.zenlauncher.scenes.ui.BubbleView;
import com.cooeeui.brand.zenlauncher.scenes.ui.ChangeIcon;
import com.cooeeui.brand.zenlauncher.scenes.ui.PopupDialog;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragLayer;

public class Launcher extends Activity implements View.OnClickListener, OnLongClickListener,
        LauncherModel.Callbacks {

    public static final String TAG = "Launcher";
    public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    SharedPreferences mSharedPrefs;
    LauncherModel mModel;
    IconCache mIconCache;

    private Workspace mWorkspace;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private ArrayList<AppInfo> mApps;

    private Dialog mLoading;

    private boolean mPaused = true;
    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();

        // the LauncherApplication should call this, but in case of
        // instrumentation it might not be present yet
        mSharedPrefs = getSharedPreferences(
                LauncherAppState.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mPaused = false;

        setContentView(R.layout.launcher);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mDragLayer.setup(this, mDragController);
        mWorkspace.setup(this, mDragController);
        mWorkspace.setOnClickListener(this);

        registerForContextMenu(mWorkspace);
        showLoadingView();

        mModel.startLoader(true);
    }

    void showLoadingView() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mLoading = new Dialog(this, R.style.LoadingViewStyle);
        mLoading.setContentView(new LoadingView(this), params);
        mLoading.setCancelable(false);
        mLoading.setCanceledOnTouchOutside(false);
        mLoading.show();
    }

    void closeLoadingView() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.dismiss();
            mLoading = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPaused = true;
        mDragController.cancelDrag();

    }

    @Override
    protected void onResume() {
        super.onResume();

        mPaused = false;
        if (mBindOnResumeCallbacks.size() > 0) {
            for (int i = 0; i < mBindOnResumeCallbacks.size(); i++) {
                mBindOnResumeCallbacks.get(i).run();
            }
            mBindOnResumeCallbacks.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                new PopupDialog(this, PopupDialog.ADD_VIEW).show();
                return true;

            case R.id.wallpaper:
                startWallpaper();
                return true;

            case R.id.settings:
                startSetting();
                return true;

            case R.id.zen:
                entryZenSetting();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.launcher_menu, menu);
        if (mWorkspace.isFull()) {
            menu.findItem(R.id.add).setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.launcher_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                new PopupDialog(this, PopupDialog.ADD_VIEW).show();
                return true;

            case R.id.wallpaper:
                startWallpaper();
                return true;

            case R.id.settings:
                startSetting();
                return true;

            case R.id.zen:
                entryZenSetting();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mWorkspace.isFull()) {
            menu.findItem(R.id.add).setVisible(false);
        } else {
            menu.findItem(R.id.add).setVisible(true);
        }
        return true;
    }

    private void startWallpaper() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        pickWallpaper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivitySafely(pickWallpaper);
    }

    private void startSetting() {
        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivitySafely(settings);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v instanceof BubbleView) {
            BubbleView view = (BubbleView) v;
            mWorkspace.startDrag(view);
        }
        return true;
    }

    boolean startActivitySafely(Intent intent) {
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found,
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        if (v instanceof Workspace) {
            mWorkspace.stopDrag();
            return;
        }

        Object tag = v.getTag();

        if (tag instanceof Integer) {
            Integer num = (Integer) v.getTag();
            switch (num.intValue()) {
                case Workspace.EDIT_VIEW_ICON:
                    new ChangeIcon(this).show();
                    break;
                case Workspace.EDIT_VIEW_CHANGE:
                    new PopupDialog(this, PopupDialog.CHANGE_VIEW).show();
                    break;
                case Workspace.EDIT_VIEW_DELETE:
                    mWorkspace.removeBubbleView();
                    break;
            }
            return;
        }

        if (tag instanceof ShortcutInfo) {
            final ShortcutInfo shortcut = (ShortcutInfo) tag;
            final Intent intent = shortcut.intent;
            if (intent != null) {
                startActivitySafely(intent);
            }
        }
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    public ArrayList<AppInfo> getApps() {
        return mApps;
    }

    @Override
    public void onBackPressed() {
        mWorkspace.stopDrag();
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        Logger.dump(writer);
    }

    private boolean waitUntilResume(Runnable run, boolean deletePreviousRunnables) {
        if (mPaused) {
            Log.i(TAG, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mBindOnResumeCallbacks.remove(run)) {
                }
            }
            mBindOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
    }

    private boolean waitUntilResume(Runnable run) {
        return waitUntilResume(run, false);
    }

    @Override
    public boolean setLoadOnResume() {
        return false;
    }

    @Override
    public void startBinding() {
        mBindOnResumeCallbacks.clear();

        mWorkspace.startBind();
    }

    @Override
    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end,
            final boolean forceAnimateIcons) {
        Runnable r = new Runnable() {
            public void run() {
                bindItems(shortcuts, start, end, forceAnimateIcons);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            ShortcutInfo info = (ShortcutInfo) item;
            mWorkspace.addBubbleViewFromBind(info);
        }
    }

    protected void onFinishBindingItems() {
        mWorkspace.finishBind();
        closeLoadingView();
    }

    @Override
    public void finishBindingItems() {
        Runnable r = new Runnable() {
            public void run() {
                finishBindingItems();
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        mWorkspace.post(new Runnable() {
            @Override
            public void run() {
                onFinishBindingItems();
            }
        });
    }

    @Override
    public void bindAllApplications(ArrayList<AppInfo> apps) {
        mApps = apps;
    }

    @Override
    public void bindAppsAdded(ArrayList<AppInfo> addedApps) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindAppsUpdated(ArrayList<AppInfo> apps) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindComponentsRemoved(ArrayList<String> packageNames, ArrayList<AppInfo> appInfos,
            boolean matchPackageNamesOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindSearchablesChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAllAppsButtonRank(int rank) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onPageBoundSynchronously(int page) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dumpLogsToLocalData() {
        // TODO Auto-generated method stub

    }

    void entryZenSetting() {
        Intent intent = new Intent(this, ZenSetting.class);
        startActivity(intent);
    }
}
