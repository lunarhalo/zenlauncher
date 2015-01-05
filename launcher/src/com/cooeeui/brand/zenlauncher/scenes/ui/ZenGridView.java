
package com.cooeeui.brand.zenlauncher.scenes.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import com.cooeeui.brand.zenlauncher.config.GridConfig;
import com.cooeeui.brand.zenlauncher.config.IconConfig;

public class ZenGridView extends GridView {

    int defaultColumns = 4;
    int countPerPage;
    boolean isOnePage;

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
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int columns = defaultColumns;
        int maxWidth = IconConfig.iconSizeMax;
        int minWidth = IconConfig.iconSizeMin;
        if (width > maxWidth * columns) {
            columns = width / maxWidth;
        } else if (width < minWidth * columns) {
            columns = width / minWidth;
        }
        setNumColumns(columns);

        int size = width / columns;
        countPerPage = columns * (height / size);
        GridConfig.setCountPerPageOfDrawer(countPerPage);

        if (isOnePage) {
            // TODO: setTranslationY();
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setDefaultColumns(int defaultColumns) {
        this.defaultColumns = defaultColumns;
    }

    public void setIsOnePage(boolean isOnePage) {
        this.isOnePage = isOnePage;
    }

    public int getCountPerPage() {
        return countPerPage;
    }
}
