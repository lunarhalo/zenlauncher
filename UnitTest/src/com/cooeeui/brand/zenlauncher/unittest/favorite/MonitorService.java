
package com.cooeeui.brand.zenlauncher.unittest.favorite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MonitorService extends Service {

    private boolean quit;

    private ActivityManager manager;

    private LocalBinder binder = new LocalBinder();

    private HashMap<ComponentName, AppData> lastApp = new HashMap<ComponentName, AppData>();

    private HashSet<String> launcherApp = new HashSet<String>();

    public class AppData {
        public ComponentName name;
        public long count;
    }

    public class LocalBinder extends Binder {
        public MonitorService getService() {
            return MonitorService.this;
        }
    }

    public HashMap<ComponentName, AppData> getLastApp() {
        Log.e("launcher123", "getLastApp lastApp size = " + lastApp.size());
        Log.e("launcher123", "getLastApp launcherApp size = " + launcherApp.size());

        return lastApp;
    }

    public void getLauncherApp() {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(launcherIntent, 0);
        String className;
        int n = apps.size();
        for (int i = 0; i < n; i++) {
            className = apps.get(i).activityInfo.name;
            launcherApp.add(className);
        }
    }

    public boolean isLauncher(ComponentName cn) {
        if (launcherApp.contains(cn.getClassName())) {
            return true;
        }

        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        quit = false;
        manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        getLauncherApp();

        new Thread() {
            @Override
            public void run() {
                while (!quit) {
                    ComponentName cn = manager.getRunningTasks(1).get(0).topActivity;
                    if (isLauncher(cn)) {

                        AppData data = lastApp.get(cn);
                        if (data == null) {
                            data = new AppData();
                            data.name = cn;
                            data.count = 1;
                            lastApp.put(cn, data);
                        } else {
                            data.count++;
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }

                Log.e("launcher123", "run exit");
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        quit = true;
        Log.e("launcher123", "Service onDestroy");
    }

}
