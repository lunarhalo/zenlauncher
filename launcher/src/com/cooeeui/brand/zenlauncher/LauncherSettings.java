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

package com.cooeeui.brand.zenlauncher;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Settings related utilities.
 */
public class LauncherSettings {
    /** Columns required on table state will be subject to backup and restore. */
    public static interface ChangeLogColumns extends BaseColumns {
        /**
         * The time of the last update to this row.
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String MODIFIED = "modified";
    }

    public static interface BaseLauncherColumns extends ChangeLogColumns {
        /**
         * Descriptive name of the gesture that can be displayed to the user.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String TITLE = "title";

        /**
         * The Intent URL of the gesture, describing what it points to. This
         * value is given to
         * {@link android.content.Intent#parseUri(String, int)} to create an
         * Intent that can be launched.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String INTENT = "intent";

        /**
         * The type of the gesture
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String ITEM_TYPE = "itemType";

        /**
         * The gesture is an application
         */
        public static final int ITEM_TYPE_APPLICATION = 0;

        /**
         * The gesture is a shortcut of application
         */
        public static final int ITEM_TYPE_SHORTCUT = 1;

        /**
         * The icon type.
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String ICON_TYPE = "iconType";

        /**
         * The icon is a resource identified by a package name and an integer
         * id.
         */
        public static final int ICON_TYPE_RESOURCE = 0;

        /**
         * The icon is a bitmap.
         */
        public static final int ICON_TYPE_BITMAP = 1;

        /**
         * The icon package name, if icon type is ICON_TYPE_RESOURCE.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String ICON_PACKAGE = "iconPackage";

        /**
         * The icon resource id, if icon type is ICON_TYPE_RESOURCE.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String ICON_RESOURCE = "iconResource";

        /**
         * The custom icon bitmap, if icon type is ICON_TYPE_BITMAP.
         * <P>
         * Type: BLOB
         * </P>
         */
        public static final String ICON = "icon";
    }

    /**
     * Applications.
     */
    public static final class Applications implements BaseLauncherColumns {
        /**
         * The category id of icon.
         * <p>
         * Type: INTEGER
         * </p>
         */
        public static final String CATEGORY = "category";

        /**
         * The priority of icon, calculate it from launch times and duration.
         * <p>
         * Type: INTEGER
         * </p>
         */
        public static final String PRIORITY = "priority";

        /**
         * The visibility of icon, 0 when visible.
         * <p>
         * Type: INTEGER
         * </p>
         */
        public static final String HIDE = "hide";

        /**
         * Whether the icon is not opened yet after installed, 1 when new.
         * <p>
         * Type: INTEGER
         * </p>
         */
        public static final String NEW = "new";

        /**
         * Whether the icon is classify, 1 when classify.
         * <p>
         * Type: INTEGER
         * </p>
         */
        public static final String CATEGORIZED = "categorized";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_APPS +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=true");

        /**
         * The content:// style URL for this table. When this Uri is used, no
         * notification is sent if the content changes.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_APPS +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=false");

        /**
         * The content:// style URL for a given row, identified by its id.
         * 
         * @param id The row id.
         * @param notify True to send a notification is the content changes.
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                    "/" + LauncherProvider.TABLE_APPS + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
    }

    /**
     * Favorites.
     */
    public static final class Favorites implements BaseLauncherColumns {
        /**
         * The position of the cell holding the favorite
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String POSITION = "position";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=true");

        /**
         * The content:// style URL for this table. When this Uri is used, no
         * notification is sent if the content changes.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=false");

        /**
         * The content:// style URL for a given row, identified by its id.
         * 
         * @param id The row id.
         * @param notify True to send a notification is the content changes.
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                    "/" + LauncherProvider.TABLE_FAVORITES + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
    }
}
