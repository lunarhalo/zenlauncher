
package com.cooeeui.brand.zenlauncher.unittest.appintent;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class AppIntentUtil {
    private Context context = null;
    private HashMap<String, ResolveInfo> allAppsMap = new HashMap<String, ResolveInfo>();
    private final String cameraName = "camera";
    private final String browserUri = "*BROWSER*";
    private final String defaultAppName = "defaultApp";

    /**
     * 配置默认浏览器的包名和类名，包名在前，类名在后，用;号隔开
     */
    private final String[] browserApps = new String[] {
            "com.tencent.mtt;com.tencent.mtt.SplashActivity",
            "com.baidu.browser.apps;com.baidu.browser.framework.BdBrowserActivity",
            "com.UCMobile;com.UCMobile.main.UCMobile",
            "com.htc.sense.browser;com.htc.sense.browser.BrowserActivity"
    };

    private final String[] defaultApps = new String[] {
            "com.example.pagedemo;com.example.pagedemo.MainActivity",
            "com.example.workspacedemo;com.example.workspacedemo.MainActivity", ""
    };

    public AppIntentUtil(Context context) {
        this.context = context;
        getAllAppList();
    }

    /**
     * 通过该intent查找该应用是否存在于该手机中
     */
    private boolean isIntentAvailable(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

    /**
     * 获得手机中所有的应用，并将类名和ResolveInfo存在allAppsMap中
     */
    private void getAllAppList() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        Iterator<ResolveInfo> ite = apps.iterator();
        ResolveInfo info = null;
        while (ite.hasNext()) {
            info = ite.next();
            if (info != null && info.loadLabel(packageManager) != null) {
                String clsName = info.activityInfo.name;
                allAppsMap.put(clsName, info);
            }

        }
    }

    /**
     * 通过一个uri获得一个应用的Intent： 获得规则：
     * 1.通过Uri直接获得intent，若此intent存在于改手机中则直接返回该intent（例如短信，联系人，通讯录可以直接获取）
     * 2.若不存在，则通过名称来匹配默认的包名和类名来查找该应用是否存在于手机中，若存在则返回该intent（主要针对照相机）
     * 3.若还不存在，则通过名称来查找手机应用的类名是否包含该名称，若查找到则返回
     * 4.对于配置项，先是匹配配置应用的包名和类名，若都查不到，则使用手机中设置的包名和类名
     * 5.若是浏览器，则先查找所有配置的包名和类名，若匹配不成功，
     * 则查找出所有的浏览器应用，让用户选择后开始记录该应用的包名和类名并保存，下次再点击的时候则直接进入该浏览器
     * 
     * @param uri
     * @param name
     * @return
     */
    public Intent getIntentByUri(String uri, String name) {
        Intent intent = null;
        if (uri != null) {
            if (uri.equals(browserUri)) {
                intent = getBrowserIntent();
            } else if (defaultAppName.equals(name)) {
                intent = getDefaultIntent(uri);
            } else {
                try {
                    intent = Intent.parseUri(uri, 0);
                    if (!isIntentAvailable(context, intent)) {
                        if (cameraName.equals(name)) {
                            intent = getAppIntent(cameraName);
                        }
                    }
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return intent;
    }

    /**
     * 获得默认配置的应用
     * 
     * @param uri
     * @return
     */
    private Intent getDefaultIntent(String uri) {
        // TODO Auto-generated method stub
        Intent intent = null;
        for (int i = 0; i < defaultApps.length; i++) {
            String pkgCls = defaultApps[i];
            intent = getIntentByPkgAndCls(pkgCls);
            if (isIntentAvailable(context, intent)) {
                return intent;
            }
        }
        if (uri != null) {
            try {
                intent = Intent.parseUri(uri, 0);
                if (!isIntentAvailable(context, intent)) {
                    intent = getAppIntent("setting");
                }

            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return intent;
    }

    /**
     * 通过包名和类名生成一个intent
     * 
     * @param pkgCls
     * @return
     */
    private Intent getIntentByPkgAndCls(String pkgCls) {
        // TODO Auto-generated method stub
        Intent intent = null;
        if (pkgCls != null) {
            String[] cp = pkgCls.split(";");
            if (cp.length > 1) {
                String pkgName = cp[0];
                String clsName = cp[1];
                ComponentName componentName = new ComponentName(pkgName, clsName);
                intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            }
        }

        return intent;
    }

    /**
     * 获得浏览器的intent
     * 
     * @return
     */
    private Intent getBrowserIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (int i = 0; i < browserApps.length; i++) {
            String pkgCls = browserApps[i];
            String[] pcs = pkgCls.split(";");
            if (pcs.length > 1) {
                ComponentName cp = new ComponentName(pcs[0], pcs[1]);
                intent.setComponent(cp);
                if (isIntentAvailable(context, intent)) {
                    return intent;
                }
            }
        }
        // 若匹配不到配置的浏览器应用，则查找出手机中所有的浏览器先，然后显示出来
        return null;
    }

    /**
     * 获得手机中关于appName（例如camera）的intent
     * 
     * @return
     */
    private Intent getAppIntent(String appName) {
        // TODO Auto-generated method stub
        Intent intent = null;
        Iterator iter = allAppsMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            if (key instanceof String && val instanceof ResolveInfo)
            {
                String clsKey = (String) key;
                ResolveInfo resolveInfo = (ResolveInfo) val;
                if (clsKey.contains(appName)) {
                    String pkgName = resolveInfo.activityInfo.packageName;
                    ComponentName cp = new ComponentName(pkgName, clsKey);
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(cp);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    return intent;
                }
            }
        }
        return intent;
    }
}
