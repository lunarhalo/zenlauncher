
package com.cooeeui.brand.zenlauncher.scenes.ui;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.category.CategoryData;
import com.cooeeui.brand.zenlauncher.config.GridConfig;
import com.cooeeui.brand.zenlauncher.scene.drawer.AppIcon;
import com.cooeeui.brand.zenlauncher.scene.drawer.ZenGridViewUtil;

public class ZenGridView extends RelativeLayout {
    private boolean isOnePage;
    private int mPosition;
    private int mTab;
    private String mName = null;

    public ZenGridView(Context context, int tab, int position) {
        super(context);
        this.mTab = tab;
        this.mPosition = position;
        this.mName = tab + "_" + position;
        ZenGridViewUtil.addAllZenGridView(this, tab, position);
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
        getIconWidthAndHeight(width, height);
        if (isOnePage) {
            // TODO: setTranslationY();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 通过整个View的宽高获得每个icon的宽高
     * 
     * @param width
     * @param height
     */
    private void getIconWidthAndHeight(int width, int height) {
        if (ZenGridViewUtil.mAllWidth != width || ZenGridViewUtil.mAllHeight != height) {
            ZenGridViewUtil.mAllWidth = width;
            ZenGridViewUtil.mAllHeight = height;
            ZenGridViewUtil.initData();
        }
    }

    public void setIsOnePage(boolean isOnePage) {
        this.isOnePage = isOnePage;
    }

    public void addChildView() {
        this.removeAllViews();
        Log.v("", "addChildView name is " + mName + " mPosition is " + mPosition);
        int count = 0;
        int size = CategoryData.getSize(mTab);
        int cpp = GridConfig.getCountPerPageOfDrawer();
        if (size > 0 && cpp > 0) {
            if ((mPosition + 1) * cpp < size) {
                count = cpp;
            } else {
                count = size % cpp;
            }
        }
        for (int position = 0; position < count; position++) {
            AppInfo info = CategoryData.datas.get(mTab).get(
                    position + GridConfig.getCountPerPageOfDrawer() * mPosition);
            if (info != null) {
                AppIcon icon = new AppIcon(getContext(), ZenGridViewUtil.mIconWidthSize,
                        ZenGridViewUtil.mIconHeightSize, info);
                LayoutParams lp = new LayoutParams(ZenGridViewUtil.mIconWidthSize,
                        ZenGridViewUtil.mIconHeightSize);
                Point point = ZenGridViewUtil.mAllPositionList.get(position);
                int cell[] = ZenGridViewUtil.getCellXAndCellY(point);
                icon.setCellX(cell[0]);
                icon.setCellY(cell[1]);
                icon.setX(point.x);
                icon.setY(point.y);
                icon.setLayoutParams(lp);
                this.addView(icon);
            }
        }
    }

    public void addZenGridChildView(View view) {
        Point point = ZenGridViewUtil.mAllPositionList.get(this.getChildCount());
        view.setX(point.x);
        view.setY(point.y);
        this.addView(view);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(200);
        view.startAnimation(alphaAnimation);

    }

    public void removeZenGridChildView(View mSelectIcon) {

        int startPosition = getPositionByView(this, mSelectIcon);
        for (int j = (startPosition + 1); j < this.getChildCount(); j++) {
            View view = this.getChildAt(j);
            Point point = ZenGridViewUtil.mAllPositionList.get(j - 1);
            view.setX(point.x);
            view.setY(point.y);
        }
        this.removeView(mSelectIcon);
    }

    private int getPositionByView(ViewGroup gridView, View mSelectIcon) {
        for (int i = 0; i < gridView.getChildCount(); i++) {
            View view = gridView.getChildAt(i);
            if (view == mSelectIcon) {
                return i;
            }
        }
        return 0;
    }

    public int getmPosition() {
        return mPosition;
    }

    public int getmTab() {
        return mTab;
    }
}
