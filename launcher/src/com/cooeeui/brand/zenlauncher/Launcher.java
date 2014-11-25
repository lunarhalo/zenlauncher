
package com.cooeeui.brand.zenlauncher;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;
import com.cooeeui.brand.zenlauncher.debug.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;

public class Launcher extends Activity implements LauncherModel.Callbacks {

    public static final String TAG = "Launcher";
    public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
    static final boolean DEBUG_STRICT_MODE = true;

    SharedPreferences mSharedPrefs;
    LauncherModel mModel;
    IconCache mIconCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork() // or
                                                                          // .detectAll()
                                                                          // for
                                                                          // all
                                                                          // detectable
                                                                          // problems
                    .penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                    .penaltyLog().penaltyDeath().build());
        }

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

        mModel.startLoader(true);
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
