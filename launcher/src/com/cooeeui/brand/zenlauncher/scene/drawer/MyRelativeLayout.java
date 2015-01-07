
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class MyRelativeLayout extends RelativeLayout {
    public int positionX = -1;
    public int positionY = -1;
    public int groupWidth = -1;
    public int groupHeight = -1;
    public String layoutName = "";
    public int spaceSizeWidth = -1;
    public int spaceModeWidth = -1;
    public int spaceModeheight = -1;
    public int spaceSizeheight = -1;

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        positionX = l;
        positionY = t;
        groupHeight = b - t;
        groupWidth = r - l;
   }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        spaceModeWidth = MeasureSpec.getMode(widthMeasureSpec);
        spaceSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        spaceModeheight = MeasureSpec.getMode(heightMeasureSpec);
        spaceSizeheight = MeasureSpec.getSize(heightMeasureSpec);
   }
}
