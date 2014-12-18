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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.util.Log;

import com.cooeeui.brand.zenlauncher.LauncherSettings;
import com.cooeeui.brand.zenlauncher.scenes.utils.IconNameOrId;

/**
 * Represents a launchable icon on the workspace.
 */
public class ShortcutInfo extends ItemInfo {

    public Bitmap mIcon;

    public boolean mRecycle;

    public int mIconId;

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
        mIcon = info.iconBitmap;
        mIconId = -1;
        mRecycle = false;
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

    @Override
    public void onAddToDatabase(ContentValues values) {
        String uri = intent != null ? intent.toUri(0) : null;
        values.put(LauncherSettings.Favorites.INTENT, uri);

        String name = IconNameOrId.getIconName(mIconId);
        values.put(LauncherSettings.Favorites.ICON_NAME, name);
    }

    @Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + "intent=" + intent + "id=" + this.id
                + " type=" + this.itemType + " position=" + this.position + ")";
    }

}
