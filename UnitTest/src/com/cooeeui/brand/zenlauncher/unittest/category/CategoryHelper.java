
package com.cooeeui.brand.zenlauncher.unittest.category;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CategoryHelper {
    public static int GAME = 0;
    public static int LIFE = 1;
    public static int TOOL = 2;
    public static int SOCIAL = 3;
    public static int SYSTEM = 4;
    public static int OTHER = 5;

    public static void init(Context context) {
        AssetsDatabaseManager.initManager(context);
    }

    public static void close() {
        AssetsDatabaseManager.closeAllDatabase();
        AssetsDatabaseManager.closeManager();
    }

    public static int getCategoryId(ApplicationInfo info) {
        AssetsDatabaseManager mgr = AssetsDatabaseManager.getManager();
        SQLiteDatabase db = mgr.getDatabase("cate.db");
        String pName = getPName(info.packageName);
        int categoryId;

        Cursor cursor = db.query("cate_offline",
                new String[] {
                    "caid"
                }, "pname=?",
                new String[] {
                    pName
                }, null, null, null);

        if (cursor.moveToFirst()) {
            String caid = cursor.getString(0);
            int caidValue = Integer.valueOf(caid) + 800;
            categoryId = getCategoryId(caidValue);
        } else {
            if (isSystem(info)) {
                categoryId = CategoryHelper.SYSTEM;
            } else {
                categoryId = CategoryHelper.OTHER;
            }
        }

        cursor.close();

        return categoryId;
    }

    /**
     * Convert package name to pname. Rewrite it use ndk in feature.
     * 
     * @param packageName
     * @return pname
     */
    private static String getPName(String packageName) {
        String pname = null;
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(packageName.getBytes());
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash)
            {
                if ((b & 0xFF) < 0x10)
                    hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            String md5 = hex.toString();
            pname = "" + Long.parseLong(md5.substring(0, 6), 16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return pname;
    }

    private static boolean isSystem(ApplicationInfo info) {
        if ((info.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                || (info.flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
            return true;
        else
            return false;
    }

    private static int getCategoryId(int caid) {
        int ret = OTHER; // the default value is OTHER if not found

        if (caid == 904 || caid == 906 || caid == 908 || caid == 909
                || caid == 910 || caid == 911 || caid == 950 || caid == -1) {
            ret = TOOL;
        } else if (caid == 902 || caid == 907) {
            ret = SOCIAL;
        } else if (caid == 914 || caid == 915 || caid == 916 || caid == 917
                || caid == 918 || caid == 919 || caid == 920 || caid == 921
                || caid == 922 || caid == 923 || caid == 924 || caid == 948
                || caid == 949) {
            ret = GAME;
        } else if (caid == 900 || caid == 901 || caid == 903 || caid == 905
                || caid == 912 || caid == 913 || caid == 951) {
            ret = LIFE;
        }

        return ret;
    }
}
