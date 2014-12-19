
package com.cooeeui.brand.zenlauncher.config;

public class IconConfig {
    public static final int ICON_SIZE_MAX = 144;
    public static final int ICON_SIZE_MIN = 72;

    static int iconSize = ICON_SIZE_MAX;;

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
