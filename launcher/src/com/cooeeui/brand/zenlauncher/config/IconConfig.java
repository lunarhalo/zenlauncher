package com.cooeeui.brand.zenlauncher.config;

public class IconConfig {
    static int iconSize;
    
    /**
     * Set the global icon size. Only called by workspace#init().
     * @param size
     */
    public static void setIconSize(int size) {
        iconSize = size;
    }
    
    public static int getIconSize() {
        return iconSize;
    }
}
