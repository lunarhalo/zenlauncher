
package com.cooeeui.brand.zenlauncher.applistlayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.R;

public class AppHostViewGroup extends MyRelativeLayout implements IAppGroup {

    private AppNameViewGroup nameViewGroup = null;
    private AppTabViewGroup tabViewGroup = null;
    private AppListViewGroup applistGroup = null;
    private boolean isApplistGroupAdded;
    private ClickButtonOnClickListener onClickListener = null;
    private AppListUtil util = null;
    private int oldWidth = groupWidth + 1;
    private int oldHeight = groupHeight + 1;
    private Launcher mLauncher;

    public AppHostViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        super.onDraw(canvas);
    }

    public void initViewData() {
        SharedPreferences preferences =
                getContext().getSharedPreferences(util.getPreferencesName(),
                        Context.MODE_PRIVATE);
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
        super.onLayout(changed, l, t, r, b);
        if (oldWidth != groupWidth || oldHeight != groupHeight) {
            oldWidth = groupWidth;
            oldHeight = groupHeight;
            addApplistGroup();
        }
    }

    private void addApplistGroup() {
        int listY = nameViewGroup.groupHeight;
        int listHeight = (this.groupHeight - nameViewGroup.groupHeight - tabViewGroup.groupHeight);
        applistGroup.setX(0);
        applistGroup.setY(listY);
        LayoutParams listLp = new LayoutParams(groupWidth, listHeight);
        applistGroup.setLayoutParams(listLp);
        applistGroup.initViewData();

        // check duplicate.
        if (!isApplistGroupAdded) {
            this.addView(applistGroup, listLp);
            isApplistGroupAdded = true;
        }
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
    }
}
