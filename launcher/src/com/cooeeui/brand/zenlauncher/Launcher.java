
package com.cooeeui.brand.zenlauncher;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;
import com.cooeeui.brand.zenlauncher.debug.Logger;

public class Launcher extends Activity implements OnLongClickListener, LauncherModel.Callbacks {

    public static final String TAG = "Launcher";
    public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    SharedPreferences mSharedPrefs;
    LauncherModel mModel;
    IconCache mIconCache;

    private Workspace mWorkspace;
    private DragLayer mDragLayer;
    private DragController mDragController;

    Button mBtn;

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

        setContentView(R.layout.launcher);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mDragLayer.setup(this, mDragController);
        mWorkspace.setup(mDragController);

        mWorkspace.setOnLongClickListener(this);

        mBtn = (Button) mWorkspace.findViewById(R.id.btn);
        mBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.e("launcher123", "onClick button");
                mWorkspace.startDrag();
            }
        });

        mModel.startLoader(true);
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        Log.e("launcher123", "onlongclick launcher");
        mWorkspace.startDrag();
        return true;
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        int childCount = mDragLayer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = mDragLayer.getChildAt(i);
            if (v instanceof DragView) {
                DragView dv = (DragView) v;
                dv.remove();
                break;
            }
        }
        Log.e("launcher123", "onBackPressed launcher childCount = " + childCount);
        // super.onBackPressed();
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        Logger.dump(writer);
    }

    @Override
    public boolean setLoadOnResume() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void startBinding() {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end,
            boolean forceAnimateIcons) {
        // TODO Auto-generated method stub

    }

    @Override
    public void finishBindingItems(boolean upgradePath) {
        // TODO Auto-generated method stub

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
    public void bindPackagesUpdated(ArrayList<Object> widgetsAndShortcuts) {
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
