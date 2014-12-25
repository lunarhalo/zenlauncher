
package com.cooeeui.brand.zenlauncher.category;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.debug.Logger;

public class CategoryData {
    public static ArrayList<ArrayList<AppInfo>> datas;

    public static void init(Context context, ArrayList<AppInfo> apps) {
        CategoryHelper.init(context);

        clear();
        makeup();

        for (AppInfo app : apps) {
            int id = CategoryHelper.getCategoryId(app);
            if (id == CategoryHelper.OTHER) {
                // Special handle for other, be regarded as tool.
                datas.get(CategoryHelper.TOOL).add(app);
            } else {
                datas.get(id).add(app);
            }
            // Logger.verbose("suyu", "compoment name = " + app.componentName +
            // ", id = " + id);
        }
    }

    public static void destroy() {
        clear();

        CategoryHelper.close();
    }

    private static void clear() {
        if (datas != null) {
            for (ArrayList<AppInfo> data : datas) {
                data.clear();
            }
            datas.clear();
        }
    }

    private static void makeup() {
        datas = new ArrayList<ArrayList<AppInfo>>();
        for (int i = 0; i < CategoryHelper.COUNT - 1; i++) {
            datas.add(new ArrayList<AppInfo>());
        }
    }
}
