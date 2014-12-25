
package com.cooeeui.brand.zenlauncher.scenes.ui;

import com.cooeeui.brand.zenlauncher.config.IconConfig;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ZenGridView extends GridView {

    private int defaultColumns = 4;

    public ZenGridView(Context context) {
        super(context);
    }

    public ZenGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZenGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int columns = defaultColumns;
        int maxWidth = IconConfig.iconSizeMax;
        int minWidth = IconConfig.iconSizeMin;
        if (width > maxWidth * columns) {
            columns = width / maxWidth;
        } else if (width < minWidth * columns) {
            columns = width / minWidth;
        }
        setNumColumns(columns);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setDefaultColumns(int defaultColumns) {
        this.defaultColumns = defaultColumns;
    }
}
