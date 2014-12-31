
package com.cooeeui.brand.zenlauncher.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.R;

public class BitmapManager {
    static BitmapManager instance;

    HashMap<String, Bitmap> bitmaps;
    HashMap<String, Integer> drawables;
    Resources resources;

    public static BitmapManager getInstance() {
        if (instance == null) {
            instance = new BitmapManager();
        }
        return instance;
    }

    public static void dispose() {
        if (instance != null) {
            instance.onDestroy();
            instance = null;
        }
    }

    BitmapManager() {
        bitmaps = new HashMap<String, Bitmap>();
        drawables = new HashMap<String, Integer>();
        resources = LauncherAppState.getInstance().getContext().getResources();

        // read in drawable data.
        readin();
    }

    // TODO: use a xml file.
    void readin() {
        drawables.put("app_hide", R.drawable.app_hide);
        drawables.put("app_uninstall", R.drawable.app_uninstall);
    }

    void onDestroy() {
        trim();

        drawables.clear();

        bitmaps = null;
        drawables = null;
        resources = null;
    }

    public void trim() {
        Iterator<Entry<String, Bitmap>> iter = bitmaps.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Bitmap> entry = iter.next();
            Bitmap bitmap = entry.getValue();
            bitmap.recycle();
        }

        bitmaps.clear();
    }

    /**
     * Get bitmap with key from manager.
     * 
     * @param key
     * @return
     */
    public Bitmap get(String key) {
        Bitmap ret = null;

        if (bitmaps.containsKey(key)) {
            ret = bitmaps.get(key);
            // remove it if is recycled.
            if (ret.isRecycled()) {
                ret = null;
                bitmaps.remove(key);
            }
        }

        if (ret == null) {
            if (drawables.containsKey(key)) {
                Bitmap bitmap = BitmapFactory.decodeResource(resources,
                        drawables.get(key));
                bitmaps.put(key, bitmap);
                ret = bitmap;
            }
        }

        return ret;
    }
}
