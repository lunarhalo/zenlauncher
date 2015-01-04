
package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.scene.drawer.AppListUtil;
import com.cooeeui.brand.zenlauncher.scene.drawer.AppListViewGroup;
import com.cooeeui.brand.zenlauncher.scene.drawer.AppTabViewGroup;
import com.cooeeui.brand.zenlauncher.scene.drawer.ClickButtonOnClickListener;
import com.cooeeui.brand.zenlauncher.scene.drawer.IAppGroup;
import com.cooeeui.brand.zenlauncher.scene.drawer.TitleBar;
import com.cooeeui.brand.zenlauncher.scenes.ui.ZenGridView;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragSource;

public class Drawer extends LinearLayout implements IAppGroup, DragSource {

    private TitleBar nameViewGroup = null;
    private AppTabViewGroup tabViewGroup = null;
    private AppListViewGroup applistGroup = null;
    private ClickButtonOnClickListener onClickListener = null;
    private AppListUtil util = null;
    private Launcher mLauncher;
    private DragController mDragController = null;

    public Drawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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

    public void initViewData() {
        SharedPreferences preferences =
                getContext().getSharedPreferences(util.getPreferencesName(),
                        Context.MODE_PRIVATE);
        int tabNum = preferences.getInt(util.gettabNumKey(), 0);
        util.setPreferences(preferences);
        util.setTabNum(tabNum);
        onClickListener = new ClickButtonOnClickListener(getContext(), util);
        // Title Bar
        nameViewGroup = (TitleBar) this.findViewById(R.id.titleBar);
        nameViewGroup.setOnClickListener(onClickListener);
        nameViewGroup.setUtil(util);
        // Tab View Group
        tabViewGroup = (AppTabViewGroup) this.findViewById(R.id.appTabGroup);
        tabViewGroup.setOnClickListener(onClickListener);
        tabViewGroup.setmDragController(mDragController);
        tabViewGroup.setUtil(util);
        tabViewGroup.initViewData();
        // App List Group
        applistGroup = (AppListViewGroup) this.findViewById(R.id.appListGroup);
        applistGroup.setmLauncher(mLauncher);
        applistGroup.setmDragController(mDragController);
        applistGroup.setTab(tabNum);
        onClickListener.setNameViewGroup(nameViewGroup);
        onClickListener.setTabViewGroup(tabViewGroup);
        onClickListener.setApplistGroup(applistGroup);
    }

    public void clickOption() {
        nameViewGroup.clickOptionButton();
    }

    public void setup(Launcher launcher, DragController dragController) {
        mLauncher = launcher;
        mDragController = dragController;
    }

    public void notifyDataSetChanged() {
        applistGroup.notifyDataSetChanged();
    }

    public void startDrag(FrameLayout v, ZenGridView parentGridView) {
        applistGroup.startDrag(this, v, parentGridView);
        tabViewGroup.startDrag();
    }

    @Override
    public void onDropCompleted(View targetView) {
        if (tabViewGroup.isTabChange()) {
            applistGroup.removeIcon();
            tabViewGroup.changeTab();
        } else {
            applistGroup.showIcon();
        }
    }
}
