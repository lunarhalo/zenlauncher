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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.util.Log;

import com.cooeeui.brand.zenlauncher.LauncherSettings;

/**
 * Represents a launchable icon on the workspace.
 */
public class ShortcutInfo extends ItemInfo {

    public boolean usingFallbackIcon;

    /**
     * The application icon.
     */
    public Bitmap mIcon;

    public boolean usingBuildinIcon;

    /**
     * The resource id if usingBuildinIcon is true.
     */
    public int mResId;

    /**
     * The time at which the application was first installed.
     */
    long firstInstallTime;

    /**
     * The flag of this application.
     */
    int flags = 0;

    @Override
    public Intent getIntent() {
        return intent;
    }

    public ShortcutInfo() {
        super();
    }

    public ShortcutInfo(AppInfo info) {
        super(info);
        intent = new Intent(info.intent);
        usingBuildinIcon = false;
        mResId = 0;
        flags = info.flags;
        firstInstallTime = info.firstInstallTime;
        mIcon = info.iconBitmap;
    }

    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.d("ShortcutInfo", "PackageManager.getPackageInfo failed for " + packageName);
        }
        return pi;
    }

    public void initFlagsAndFirstInstallTime(PackageInfo pi) {
        flags = AppInfo.initFlags(pi);
        firstInstallTime = AppInfo.initFirstInstallTime(pi);
    }

    /**
     * Creates the application intent based on a component name and various
     * launch flags. Sets {@link #itemType} to
     * {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     * 
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(Context context, ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
        initFlagsAndFirstInstallTime(getPackageInfo(context, intent.getComponent().getPackageName()));
    }

    @Override
    public void onAddToDatabase(ContentValues values) {
        String titleStr = title != null ? title.toString() : null;
        values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);

        String uri = intent != null ? intent.toUri(0) : null;
        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);

        values.put(LauncherSettings.Favorites.POSITION, position);
        if (usingBuildinIcon) {
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE, mResId);
        }
    }

    public void updateValuesWithPosition(ContentValues values, int position) {
        values.put(LauncherSettings.Favorites.POSITION, position);
    }

    @Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + "intent=" + intent + "id=" + this.id
                + " type=" + this.itemType + " position=" + this.position + ")";
    }

}
