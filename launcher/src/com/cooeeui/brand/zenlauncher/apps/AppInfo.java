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

package com.cooeeui.brand.zenlauncher.apps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import com.cooeeui.brand.zenlauncher.LauncherSettings;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfo {
    private static final String TAG = "ZenLauncher.AppInfo";

    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;

    /**
     * Whether the application is opened at least once.
     */
    public boolean isOpened;

    /**
     * The priority of the application on favorite page.
     */
    public long priority;

    /**
     * Whether the application is hide.
     */
    public boolean hide;

    /**
     * The time at which the application was first installed.
     */
    public long firstInstallTime;

    /**
     * The category of the application.
     */
    public int category;

    public ComponentName componentName;

    public static final int DOWNLOADED_FLAG = 1;
    public static final int UPDATED_SYSTEM_APP_FLAG = 2;
    /**
     * The flag of this application.
     */
    public int flags = 0;

    @Override
    public Intent getIntent() {
        return intent;
    }

    public AppInfo(AppInfo info) {
        super(info);
        intent = new Intent(info.intent);
        // TODO: How do we handle the icon bitmap?
        isOpened = info.isOpened;
        priority = info.priority;
        hide = info.hide;
        firstInstallTime = info.firstInstallTime;
        componentName = info.componentName;
        flags = info.flags;
        category = info.category;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(PackageManager pm, ResolveInfo info, IconCache iconCache,
            HashMap<Object, CharSequence> labelCache) {
        final String packageName = info.activityInfo.applicationInfo.packageName;

        this.componentName = new ComponentName(packageName, info.activityInfo.name);
        this.setActivity(componentName,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            flags = initFlags(pi);
            firstInstallTime = initFirstInstallTime(pi);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "PackageManager.getApplicationInfo failed for " + packageName);
        }

        iconCache.getTitleAndIcon(this, info, labelCache);
    }

    /**
     * Return the flags by package info.
     */
    public static int initFlags(PackageInfo pi) {
        int appFlags = pi.applicationInfo.flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;

            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    /**
     * Return the first install time of a package info.
     */
    public static long initFirstInstallTime(PackageInfo pi) {
        return pi.firstInstallTime;
    }

    /**
     * Creates the application intent based on a component name and various
     * launch flags. Sets {@link #itemType} to
     * {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     * 
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + this.title.toString() + " id=" + this.id
                + " type=" + this.itemType + " priority=" + this.priority
                + " category=" + this.category + " componentName=" + this.componentName + ")";
    }

    public static void dumpApplicationInfoList(String tag, String label, ArrayList<AppInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (AppInfo info : list) {
            Log.d(tag, "   title=\"" + info.title + "\" iconBitmap="
                    + info.iconBitmap + " firstInstallTime="
                    + info.firstInstallTime);
        }
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }
}
