
package com.cooeeui.brand.zenlauncher.applistlayout;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

public class AppListUtil {
    public final String[] tabName = new String[] {
            "Favourite", "Communication", "Life", "Soclal", "System", "Tool"
    };
    public final String optionName = "optionName";
    private Context context = null;
    private int allScreenWidth = -1;
    private int allScreenHeight = -1;
    private final int lineWidth = 2;
    private final int tabWidth = 2;
    private int oldList = -1;
    private final String preferencesName = "ApplistLayoutName";
    private final String tabNumKey = "tabNum";
    private int tabNum = 0;
    private SharedPreferences preferences = null;
    public SharedPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public int getTabNum() {
        return tabNum;
    }

    public void setTabNum(int tabNum) {
        this.tabNum = tabNum;
    }

    public String getPreferencesName() {
        return preferencesName;
    }

    public String gettabNumKey() {
        return tabNumKey;
    }

    public int getOldList() {
        return oldList;
    }

    public void setOldList(int oldList) {
        this.oldList = oldList;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    private float density = 1f;

    public float getDensity() {
        return density;
    }

    public int getAllScreenWidth() {
        return allScreenWidth;
    }

    public int getAllScreenHeight() {
        return allScreenHeight;
    }

    public AppListUtil(Activity activity) {
        this.context = activity;
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
        allScreenWidth = metrics.widthPixels;
        allScreenHeight = metrics.heightPixels;
    }
}
