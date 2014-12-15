
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import com.cooeeui.brand.zenlauncher.unittest.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

public class AppHostViewGroup extends ViewGroup {

    private AppNameViewGroup nameViewGroup = null;
    private AppTabViewGroup tabViewGroup = null;
    private AppListViewGroup applistGroup = null;
    private ClickButtonOnClickListener onClickListener = null;

    public AppHostViewGroup(Context context, AppListUtil util) {
        super(context);
        this.setBackgroundResource(R.drawable.applayout_wallpaper);
        SharedPreferences preferences = context.getSharedPreferences(util.getPreferencesName(),
                context.MODE_PRIVATE);
        int tabNum = preferences.getInt(util.gettabNumKey(), 0);
        util.setPreferences(preferences);
        util.setTabNum(tabNum);

        onClickListener = new ClickButtonOnClickListener(getContext(), util);
        nameViewGroup = new AppNameViewGroup(context, util, onClickListener);
        int nameHeight = (int) (util.getAllScreenHeight() * 0.07f);
        nameViewGroup.setLeft(0);
        nameViewGroup.setTop(0);
        nameViewGroup.setRight(util.getAllScreenWidth());
        nameViewGroup.setBottom(nameHeight);
        this.addView(nameViewGroup);
        nameViewGroup.initAddChildView();
        tabViewGroup = new AppTabViewGroup(context, util, onClickListener);
        int tabHeight = (int) (util.getAllScreenHeight() * 0.15f);
        tabViewGroup.setLeft(0);
        tabViewGroup.setTop(util.getAllScreenHeight() - tabHeight);
        tabViewGroup.setRight(util.getAllScreenWidth());
        tabViewGroup.setBottom(util.getAllScreenHeight());
        this.addView(tabViewGroup);
        tabViewGroup.initAddChildView();

        applistGroup = new AppListViewGroup(context, util);
        int apptop = nameViewGroup.getHeight();
        int appbottom = tabViewGroup.getTop();
        applistGroup.setLeft(0);
        applistGroup.setTop(apptop);
        applistGroup.setRight(util.getAllScreenWidth());
        applistGroup.setBottom(appbottom);
        applistGroup.initAddChildView();
        this.addView(applistGroup);
        onClickListener.setNameViewGroup(nameViewGroup);
        onClickListener.setTabViewGroup(tabViewGroup);
        onClickListener.setApplistGroup(applistGroup);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
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

}
