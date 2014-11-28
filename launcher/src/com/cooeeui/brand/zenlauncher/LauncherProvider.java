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
import java.net.URISyntaxException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.Settings;
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
    static final String PARAMETER_NOTIFY = "notify";
    static final String UPGRADED_FROM_OLD_DATABASE =
            "UPGRADED_FROM_OLD_DATABASE";
    static final String EMPTY_DATABASE_CREATED =
            "EMPTY_DATABASE_CREATED";
    static final String DEFAULT_WORKSPACE_RESOURCE_ID =
            "DEFAULT_WORKSPACE_RESOURCE_ID";

    private DatabaseHelper mOpenHelper;
    private static boolean sJustLoadedFromOldDb;

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

    private static long dbInsertAndCheck(DatabaseHelper helper,
            SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
        if (!values.containsKey(LauncherSettings.Favorites._ID)) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        return db.insert(table, nullColumnHack, values);
    }

    private static void deleteId(SQLiteDatabase db, long id) {
        Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        SqlArguments args = new SqlArguments(uri, null, null);
        db.delete(args.table, args.where, args.args);
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
     * @param Should we load the old db for upgrade? first run only.
     */
    synchronized public boolean justLoadedOldDb() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);

        boolean loadedOldDb = false || sJustLoadedFromOldDb;

        sJustLoadedFromOldDb = false;
        if (sp.getBoolean(UPGRADED_FROM_OLD_DATABASE, false)) {

            SharedPreferences.Editor editor = sp.edit();
            editor.remove(UPGRADED_FROM_OLD_DATABASE);
            editor.commit();
            loadedOldDb = true;
        }
        return loadedOldDb;
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
            mOpenHelper.setFlagJustLoadedOldDb();
            editor.commit();
        }
    }

    private static interface ContentValuesCallback {
        public void onRow(ContentValues values);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_SHORTCUT = "shortcut";

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

            db.execSQL("CREATE TABLE favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "intent TEXT," +
                    "container INTEGER," +
                    "cellIndex INTEGER," +
                    "itemType INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "uri TEXT," +
                    "modified INTEGER NOT NULL DEFAULT 0" +
                    ");");

            // Try converting the old database
            ContentValuesCallback permuteScreensCb = new ContentValuesCallback() {
                public void onRow(ContentValues values) {
                    // The database is the first version, Do nothing.
                }
            };
            Uri uri = Uri.parse("content://" + Settings.AUTHORITY +
                    "/old_favorites?notify=true");
            if (!convertDatabase(db, uri, permuteScreensCb, true)) {
                // If we fail, then set a flag to load the default workspace
                setFlagEmptyDbCreated();
                return;
            }
            // Right now, in non-default workspace cases, we want to run the
            // final upgrade code (ie. to fix workspace screen indices -> ids,
            // etc.), so set that flag too.
            setFlagJustLoadedOldDb();
        }

        private void setFlagJustLoadedOldDb() {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(UPGRADED_FROM_OLD_DATABASE, true);
            editor.putBoolean(EMPTY_DATABASE_CREATED, false);
            editor.commit();
        }

        private void setFlagEmptyDbCreated() {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(EMPTY_DATABASE_CREATED, true);
            editor.putBoolean(UPGRADED_FROM_OLD_DATABASE, false);
            editor.commit();
        }

        private boolean convertDatabase(SQLiteDatabase db, Uri uri,
                ContentValuesCallback cb, boolean deleteRows) {
            if (LOGD)
                Log.d(TAG, "converting database from an older format, but not onUpgrade");
            boolean converted = false;

            final ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;

            try {
                cursor = resolver.query(uri, null, null, null, null);
            } catch (Exception e) {
                // Ignore
            }

            // We already have a favorites database in the old provider
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        converted = copyFromCursor(db, cursor, cb) > 0;
                        if (converted && deleteRows) {
                            resolver.delete(uri, null, null);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }

            if (converted) {
                // Update max item id
                mMaxItemId = initializeMaxItemId(db);
                if (LOGD)
                    Log.d(TAG, "mMaxItemId: " + mMaxItemId);
            }

            return converted;
        }

        private int copyFromCursor(SQLiteDatabase db, Cursor c, ContentValuesCallback cb) {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int containerIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int index = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INDEX);
            final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);

            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put(LauncherSettings.Favorites._ID, c.getLong(idIndex));
                values.put(LauncherSettings.Favorites.INTENT, c.getString(intentIndex));
                values.put(LauncherSettings.Favorites.TITLE, c.getString(titleIndex));
                values.put(LauncherSettings.Favorites.ICON_TYPE, c.getInt(iconTypeIndex));
                values.put(LauncherSettings.Favorites.ICON, c.getBlob(iconIndex));
                values.put(LauncherSettings.Favorites.ICON_PACKAGE, c.getString(iconPackageIndex));
                values.put(LauncherSettings.Favorites.ICON_RESOURCE, c.getString(iconResourceIndex));
                values.put(LauncherSettings.Favorites.CONTAINER, c.getInt(containerIndex));
                values.put(LauncherSettings.Favorites.ITEM_TYPE, c.getInt(itemTypeIndex));
                values.put(LauncherSettings.Favorites.INDEX, c.getInt(index));
                values.put(LauncherSettings.Favorites.URI, c.getString(uriIndex));
                if (cb != null) {
                    cb.onRow(values);
                }
                rows[i++] = values;
            }

            int total = 0;
            if (i > 0) {
                db.beginTransaction();
                try {
                    int numValues = rows.length;
                    for (i = 0; i < numValues; i++) {
                        if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, rows[i]) < 0) {
                            return 0;
                        } else {
                            total++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            return total;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (LOGD)
                Log.d(TAG, "onUpgrade triggered: " + oldVersion);

            int version = oldVersion;

            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);

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
         */
        private int loadFavorites(SQLiteDatabase db, int workspaceResourceId) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ContentValues values = new ContentValues();

            if (LOGD)
                Log.v(TAG,
                        String.format("Loading favorites from resid=0x%08x", workspaceResourceId));

            PackageManager packageManager = mContext.getPackageManager();
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

                    long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                    if (a.hasValue(R.styleable.Favorite_container)) {
                        container = Long.valueOf(a.getString(R.styleable.Favorite_container));
                    }

                    String index = a.getString(R.styleable.Favorite_index);

                    values.clear();
                    values.put(LauncherSettings.Favorites.CONTAINER, container);
                    values.put(LauncherSettings.Favorites.INDEX, index);

                    if (LOGD) {
                        final String title = a.getString(R.styleable.Favorite_title);
                        final String pkg = a.getString(R.styleable.Favorite_packageName);
                        final String something = title != null ? title : pkg;
                        Log.v(TAG, String.format(
                                ("%" + (2 * (depth + 1)) + "s<%s%s c=%d i=%s>"),
                                "", name,
                                (something == null ? "" : (" \"" + something + "\"")),
                                container, index));
                    }

                    if (TAG_FAVORITE.equals(name)) {
                        long id = addAppShortcut(db, values, a, packageManager, intent);
                        added = id >= 0;
                    } else if (TAG_SHORTCUT.equals(name)) {
                        long id = addUriShortcut(db, values, a);
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

        private long addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager, Intent intent) {
            long id = -1;
            ActivityInfo info;
            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                    info = packageManager.getActivityInfo(cn, 0);
                } catch (PackageManager.NameNotFoundException nnfe) {
                    String[] packages = packageManager.currentToCanonicalPackageNames(
                            new String[] {
                                    packageName
                            });
                    cn = new ComponentName(packages[0], className);
                    info = packageManager.getActivityInfo(cn, 0);
                }
                id = generateNewItemId();
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                values.put(Favorites.INTENT, intent.toUri(0));
                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
                values.put(Favorites._ID, generateNewItemId());
                if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
                    return -1;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Unable to add favorite: " + packageName +
                        "/" + className, e);
            }
            return id;
        }

        private long addUriShortcut(SQLiteDatabase db, ContentValues values,
                TypedArray a) {
            Resources r = mContext.getResources();

            final int iconResId = a.getResourceId(R.styleable.Favorite_icon, 0);
            final int titleResId = a.getResourceId(R.styleable.Favorite_title, 0);

            Intent intent;
            String uri = null;
            try {
                uri = a.getString(R.styleable.Favorite_uri);
                intent = Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                Log.w(TAG, "Shortcut has malformed uri: " + uri);
                return -1; // Oh well
            }

            if (iconResId == 0 || titleResId == 0) {
                Log.w(TAG, "Shortcut is missing title or icon resource ID");
                return -1;
            }

            long id = generateNewItemId();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.TITLE, r.getString(titleResId));
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
            values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
            values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
            values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));
            values.put(Favorites._ID, id);

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
