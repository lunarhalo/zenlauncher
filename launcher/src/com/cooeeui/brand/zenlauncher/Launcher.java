
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
import com.cooeeui.brand.zenlauncher.scenes.ui.BubbleView;
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

    Dialog mLoading;

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

        mWorkspace.setOnLongClickListener(this);

        // showLoadingView();

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
        // closeLoadingView();
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
    public boolean onLongClick(View v) {
        if (v instanceof Workspace) {
            // mWorkspace.startDrag();
        } else if (v instanceof BubbleView) {
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
        Object tag = v.getTag();
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

    @Override
    public void onBackPressed() {
        int childCount = mWorkspace.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = mWorkspace.getChildAt(i);
            if (v instanceof BubbleView) {
                BubbleView bv = (BubbleView) v;
                mWorkspace.removeView(bv);
                mWorkspace.removeBubbleView(bv);
                mDragController.removeDropTarget(bv);
                mWorkspace.update();
                break;
            }
        }

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
        // TODO Auto-generated method stub

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

}
