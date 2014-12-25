
package com.cooeeui.brand.zenlauncher.config;

import com.cooeeui.brand.zenlauncher.utils.DensityUtil;

import android.content.Context;

public class IconConfig {
    public static final int ICON_SIZE_MAX_DP = 72;
    public static final int ICON_SIZE_MIN_DP = 36;
    public static int iconSizeMax;
    public static int iconSizeMin;

    static int iconSize;
    
    public static void init(Context context) {
        iconSizeMax = DensityUtil.dip2px(context, ICON_SIZE_MAX_DP);
        iconSizeMin = DensityUtil.dip2px(context, ICON_SIZE_MIN_DP);
        
        iconSize = iconSizeMax;
    }

    /**
     * Set the global icon size. Only called by workspace#init().
     * 
     * @param size
     */
    public static void setIconSize(int size) {
        iconSize = size;
    }

    public static int getIconSize() {
        return iconSize;
    }
}
