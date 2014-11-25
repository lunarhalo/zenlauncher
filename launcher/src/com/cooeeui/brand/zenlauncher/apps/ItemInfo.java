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
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.LauncherSettings;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {

    public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT}
     */
    public int itemType;

    /**
     * The id of the container that holds this item. For the desktop, this will
     * be {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the
     * applications it will be
     * {@link LauncherSettings.Favorites#CONTAINER_PAGE_SYSTEM} or other pages.
     */
    public long container = NO_ID;

    /**
     * Indicates the index of the associated cell.
     */
    public int index = -1;

    /**
     * Indicates that this item needs to be updated in the db
     */
    public boolean requiresDbUpdate = false;

    /**
     * Title of the item
     */
    public CharSequence title;

    public ItemInfo() {
    }

    ItemInfo(ItemInfo info) {
        id = info.id;
        itemType = info.itemType;
        container = info.container;
        index = info.index;
        // temporary debug:
        LauncherModel.checkItemInfo(this);
    }

    public Intent getIntent() {
        throw new RuntimeException("Unexpected Intent");
    }

    /**
     * Write the fields of this item to the DB
     * 
     * @param values
     */
    public void onAddToDatabase(ContentValues values) {
        values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, itemType);
        values.put(LauncherSettings.Favorites.CONTAINER, container);
        values.put(LauncherSettings.Favorites.INDEX, index);
    }

    public void updateValuesWithIndex(ContentValues values, int index) {
        values.put(LauncherSettings.Favorites.INDEX, index);
    }

    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(LauncherSettings.Favorites.ICON, data);
        }
    }

    /**
     * It is very important that sub-classes implement this if they contain any
     * references to the activity (anything in the view hierarchy etc.). If not,
     * leaks can result since ItemInfo objects persist across rotation and can
     * hence leak by holding stale references to the old view hierarchy /
     * activity.
     */
    public void unbind() {
    }

    @Override
    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container
                + " index=" + index + ")";
    }
}
