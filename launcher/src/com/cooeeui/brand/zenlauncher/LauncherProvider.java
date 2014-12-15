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

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.cooeeui.brand.zenlauncher.LauncherSettings.Favorites;
import com.cooeeui.brand.zenlauncher.config.ProviderConfig;

public class LauncherProvider extends ContentProvider {
    private static final String TAG = "Launcher.LauncherProvider";
    private static final boolean LOGD = false;

    private static final String DATABASE_NAME = "launcher.db";

    private static final int DATABASE_VERSION = 1;

    static final String AUTHORITY = ProviderConfig.AUTHORITY;

    static final String TABLE_FAVORITES = "favorites";
    static final String TABLE_APPS = "apps";
    static final String PARAMETER_NOTIFY = "notify";
    static final String EMPTY_DATABASE_CREATED =
            "EMPTY_DATABASE_CREATED";
    static final String DEFAULT_WORKSPACE_RESOURCE_ID =
            "DEFAULT_WORKSPACE_RESOURCE_ID";

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new DatabaseHelper(context);
        LauncherAppState.setLauncherProvider(this);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        addModifiedTime(initialValues);
        final long rowId = dbInsertAndCheck(mOpenHelper, db, args.table, null, initialValues);
        if (rowId <= 0)
            return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                addModifiedTime(values[i]);
                if (dbInsertAndCheck(mOpenHelper, db, args.table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0)
            sendNotify(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        addModifiedTime(values);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0)
            sendNotify(uri);

        return count;
    }

    /**
     * Insert values to database with _id column check.
     */
    private static long dbInsertAndCheck(DatabaseHelper helper,
            SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
        if (!values.containsKey(LauncherSettings.Favorites._ID)) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        return db.insert(table, nullColumnHack, values);
    }

    /**
     * Delete a shortcut from table favorites by id.
     * 
     * @param db
     * @param id id of shortcut
     */
    private static void deleteId(SQLiteDatabase db, long id) {
        Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        SqlArguments args = new SqlArguments(uri, null, null);
        db.delete(args.table, args.where, args.args);
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private void addModifiedTime(ContentValues values) {
        values.put(LauncherSettings.ChangeLogColumns.MODIFIED, System.currentTimeMillis());
    }

    public long generateNewItemId() {
        return mOpenHelper.generateNewItemId();
    }

    public void updateMaxItemId(long id) {
        mOpenHelper.updateMaxItemId(id);
    }

    /**
     * @param workspaceResId that can be 0 to use default or non-zero for
     *            specific resource
     */
    synchronized public void loadDefaultFavoritesIfNecessary(int origWorkspaceResId) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);

        if (sp.getBoolean(EMPTY_DATABASE_CREATED, false)) {
            int workspaceResId = origWorkspaceResId;

            // Use default workspace resource if none provided
            if (workspaceResId == 0) {
                workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.default_workspace);
            }

            // Populate favorites table with initial favorites
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(EMPTY_DATABASE_CREATED);
            if (origWorkspaceResId != 0) {
                editor.putInt(DEFAULT_WORKSPACE_RESOURCE_ID, origWorkspaceResId);
            }

            mOpenHelper.loadFavorites(mOpenHelper.getWritableDatabase(), workspaceResId);
            editor.commit();
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite";

        private final Context mContext;
        private long mMaxItemId = -1;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;

            // In the case where neither onCreate nor onUpgrade gets called, we
            // read the maxId from
            // the DB here
            if (mMaxItemId == -1) {
                mMaxItemId = initializeMaxItemId(getWritableDatabase());
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (LOGD)
                Log.d(TAG, "creating new launcher database");

            mMaxItemId = 1;

            // Create table favorites.
            db.execSQL("CREATE TABLE favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "intent TEXT," +
                    "position INTEGER," +
                    "itemType INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconName TEXT," +
                    "icon BLOB," +
                    "uri TEXT," +
                    "modified INTEGER NOT NULL DEFAULT 0" +
                    ");");

            // Create table apps.
            db.execSQL("CREATE TABLE apps (" +
                    "_id INTEGER PRIMARY KEY," +
                    "component TEXT," +
                    "priority INTEGER," +
                    "category INTEGER," +
                    "hide INTEGER," +
                    "opened INTEGER," +
                    "modified INTEGER NOT NULL DEFAULT 0" +
                    ");");
            setFlagEmptyDbCreated();
        }

        private void setFlagEmptyDbCreated() {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(EMPTY_DATABASE_CREATED, true);
            editor.commit();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (LOGD)
                Log.d(TAG, "onUpgrade triggered: " + oldVersion);

            int version = oldVersion;

            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS);

                onCreate(db);
            }
        }

        // Generates a new ID to use for an object in your database. This method
        // should be only called from the main UI thread. As an exception, we do
        // call it when we call the constructor from the worker thread; however,
        // this doesn't extend until after the constructor is called, and we
        // only pass a reference to LauncherProvider to LauncherApp after that
        // point
        public long generateNewItemId() {
            if (mMaxItemId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            mMaxItemId += 1;
            return mMaxItemId;
        }

        public void updateMaxItemId(long id) {
            mMaxItemId = id + 1;
        }

        private long initializeMaxItemId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM favorites", null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max item id");
            }

            return id;
        }

        private static final void beginDocument(XmlPullParser parser, String firstElementName)
                throws XmlPullParserException, IOException {
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            if (!parser.getName().equals(firstElementName)) {
                throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                        ", expected " + firstElementName);
            }
        }

        /**
         * Loads the default set of favorite packages from an xml file.
         * 
         * @param db The database to write the values into
         * @param workspaceResourceId the xml resource id, chould not be 0
         * @return the item count be loaded
         */
        private int loadFavorites(SQLiteDatabase db, int workspaceResourceId) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ContentValues values = new ContentValues();
            if (LOGD)
                Log.v(TAG,
                        String.format("Loading favorites from resid=0x%08x", workspaceResourceId));

            int i = 0;
            try {
                XmlResourceParser parser = mContext.getResources().getXml(workspaceResourceId);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                beginDocument(parser, TAG_FAVORITES);
                final int depth = parser.getDepth();
                int type;
                while (((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }
                    boolean added = false;
                    final String name = parser.getName();
                    // Assuming it's a <favorite> at this point
                    TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);

                    String position = a.getString(R.styleable.Favorite_position);

                    values.clear();
                    values.put(LauncherSettings.Favorites.POSITION, position);

                    if (LOGD) {
                        final String title = a.getString(R.styleable.Favorite_title);
                        final String pkg = a.getString(R.styleable.Favorite_packageName);
                        final String something = title != null ? title : pkg;
                        Log.v(TAG, String.format(
                                ("%" + (2 * (depth + 1)) + "s<%s%s pos=%s>"),
                                "", name,
                                (something == null ? "" : (" \"" + something + "\"")),
                                ""));
                    }
                    if (TAG_FAVORITE.equals(name)) {
                        long id = addAppShortcut(db, values, a);
                        added = id >= 0;
                    }
                    if (added)
                        i++;
                    a.recycle();
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (RuntimeException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            }
            // Update the max item id after we have loaded the database
            if (mMaxItemId == -1) {
                mMaxItemId = initializeMaxItemId(db);
            }
            return i;
        }

        private long addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a) {

            long id = -1;
            String intentName = a.getString(R.styleable.Favorite_intent);
            String position = a.getString(R.styleable.Favorite_position);
            String iconName = a.getString(R.styleable.Favorite_iconName);
            String pkgName = mContext.getPackageName();
            values.put(Favorites.ICON_PACKAGE, pkgName);
            values.put(Favorites.ICON_NAME, iconName);
            values.put(Favorites.POSITION, position);
            values.put(Favorites.INTENT, intentName);
            values.put(Favorites._ID, generateNewItemId());
            if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
                return -1;
            }
            return id;
        }

    }

    /**
     * Build a query string that will match any row where the column matches
     * anything in the values list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
