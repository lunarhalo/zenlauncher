
package com.cooeeui.brand.zenlauncher.unittest.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class SuperGridView extends GridView {

    public SuperGridView(Context context) {
        super(context);
    }

    public SuperGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int columns = 4;
        int maxWidth = dip2px(72);
        int minWidth = dip2px(48);
        if (width > maxWidth * columns) {
            columns = width / maxWidth;
        } else if (width < minWidth * columns) {
            columns = width / minWidth;
        }
        setNumColumns(columns);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
