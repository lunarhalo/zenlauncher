
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.cooeeui.brand.zenlauncher.unittest.R;

public class AppHostViewGroup extends MyRelativeLayout implements IAppGroup {

    private AppNameViewGroup nameViewGroup = null;
    private AppTabViewGroup tabViewGroup = null;
    private AppListViewGroup applistGroup = null;
    private ClickButtonOnClickListener onClickListener = null;
    private AppListUtil util = null;
    private int oldWidth = groupWidth + 1;
    private int oldHeight = groupHeight + 1;

    public AppHostViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        // TODO Auto-generated constructor stub
    }

    public AppNameViewGroup getNameViewGroup() {
        return nameViewGroup;
    }

    public void setNameViewGroup(AppNameViewGroup nameViewGroup) {
        this.nameViewGroup = nameViewGroup;
    }

    public AppTabViewGroup getTabViewGroup() {
        return tabViewGroup;
    }

    public void setTabViewGroup(AppTabViewGroup tabViewGroup) {
        this.tabViewGroup = tabViewGroup;
    }

    public AppListViewGroup getApplistGroup() {
        return applistGroup;
    }

    public void setApplistGroup(AppListViewGroup applistGroup) {
        this.applistGroup = applistGroup;
    }

    public ClickButtonOnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(ClickButtonOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public AppListUtil getUtil() {
        return util;
    }

    public void setUtil(AppListUtil util) {
        this.util = util;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
    }

    @Override
    public void initViewData() {
        // TODO Auto-generated method stub
        SharedPreferences preferences =
                getContext().getSharedPreferences(util.getPreferencesName(),
                        getContext().MODE_PRIVATE);
        int tabNum = preferences.getInt(util.gettabNumKey(), 0);
        util.setPreferences(preferences);
        util.setTabNum(tabNum);
        onClickListener = new ClickButtonOnClickListener(getContext(), util);
        nameViewGroup = (AppNameViewGroup) this.findViewById(R.id.appNameGroup);
        nameViewGroup.setOnClickListener(onClickListener);
        nameViewGroup.setUtil(util);
        nameViewGroup.initViewData();
        tabViewGroup = (AppTabViewGroup) this.findViewById(R.id.appTabGroup);
        tabViewGroup.setOnClickListener(onClickListener);
        tabViewGroup.setUtil(util);
        tabViewGroup.initViewData();
        applistGroup = new AppListViewGroup(getContext(), util);
        onClickListener.setNameViewGroup(nameViewGroup);
        onClickListener.setTabViewGroup(tabViewGroup);
        onClickListener.setApplistGroup(applistGroup);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        if (oldWidth != groupWidth || oldHeight != groupHeight) {
            oldWidth = groupWidth;
            oldHeight = groupHeight;
            addApplistGroup();
        }
    }

    private void addApplistGroup() {
        // TODO Auto-generated method stub
        int listY = nameViewGroup.groupHeight;
        int listHeight = (this.groupHeight - nameViewGroup.groupHeight - tabViewGroup.groupHeight);
        applistGroup.setX(0);
        applistGroup.setY(listY);
        LayoutParams listLp = new LayoutParams(groupWidth, listHeight);
        this.addView(applistGroup, listLp);
        applistGroup.initViewData();
    }

}
