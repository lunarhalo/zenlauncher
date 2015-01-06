
package com.cooeeui.brand.zenlauncher.utils;

import android.graphics.Rect;

public class MathUtils {
    public static Rect scale(Rect rect, float scale) {
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        rect.set((int) (centerX - rect.width() / 2 * scale),
                (int) (centerY - rect.height() / 2 * scale),
                (int) (centerX + rect.width() / 2 * scale),
                (int) (centerY + rect.height() / 2 * scale));
        return rect;
    }
}
