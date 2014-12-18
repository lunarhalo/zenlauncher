
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AppListViewGroup extends ViewGroup implements IAppGroup {
    private TextView testView = null;
    private AppListUtil util = null;

    public AppListViewGroup(Context context, AppListUtil util) {
        super(context);
        this.util = util;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        for (int i = 0; i < this.getChildCount(); i++) {
            View view = this.getChildAt(i);
            int viewleft = view.getLeft();
            int viewtop = view.getTop();
            int viewright = view.getRight();
            int viewbottom = view.getBottom();
            view.layout(viewleft, viewtop, viewright, viewbottom);
        }
    }

    public void changeTextView(String text) {
        if (testView != null) {
            testView.setText(text);
        }
    }

    @Override
    public void initViewData() {
        // TODO Auto-generated method stub
        testView = new TextView(getContext());
        int left = util.getAllScreenWidth() / 3;
        int top = util.getAllScreenHeight() / 3;
        int right = left * 3;
        int bottom = top * 2;
        testView.setLeft(left);
        testView.setTop(top);
        testView.setRight(right);
        testView.setBottom(bottom);
        testView.setText(util.tabName[util.getTabNum()]);
        testView.setTextSize(20);
        this.addView(testView);
    }
}
