
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.cooeeui.brand.zenlauncher.R;

public class ClickButtonOnClickListener implements OnClickListener {

    private Context context = null;
    private TitleBar titleBar = null;
    private AppListViewGroup applistGroup = null;
    private AppTabViewGroup tabViewGroup = null;
    private AppListUtil util = null;
    private PopMenuGroup mPopMenuGroup = null;
    private PopupWindow mPopupWindow = null;

    public PopMenuGroup getmPopMenuGroup() {
        return mPopMenuGroup;
    }

    public void setmPopMenuGroup(PopMenuGroup mPopMenuGroup) {
        this.mPopMenuGroup = mPopMenuGroup;
    }

    public AppListViewGroup getApplistGroup() {
        return applistGroup;
    }

    public void setApplistGroup(AppListViewGroup applistGroup) {
        this.applistGroup = applistGroup;
    }

    public TitleBar getNameViewGroup() {
        return titleBar;
    }

    public void setNameViewGroup(TitleBar nameViewGroup) {
        this.titleBar = nameViewGroup;
    }

    public AppTabViewGroup getTabViewGroup() {
        return tabViewGroup;
    }

    public void setTabViewGroup(AppTabViewGroup tabViewGroup) {
        this.tabViewGroup = tabViewGroup;
    }

    public ClickButtonOnClickListener(Context context, AppListUtil util) {
        this.context = context;
        this.util = util;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof String) {
            String nameTag = (String) tag;
            doneChangeByValue(nameTag, v);
        }
    }

    public void doneChangeByValue(String nameTag, View v) {
        if (nameTag.equals(util.optionName)) {
            doneSomethingInOption(v);
        } else if (mPopMenuGroup != null && (nameTag.contains(mPopMenuGroup.mPopTag))) {
            if (nameTag.equals(context.getResources().getString(R.string.classify)
                    + mPopMenuGroup.mPopTag)) {
                applistGroup.classifyApp();
            } else if (nameTag.equals(context.getResources().getString(R.string.unload)
                    + mPopMenuGroup.mPopTag)) {
                applistGroup.unloadApp();
            } else if (nameTag.equals(context.getResources().getString(R.string.hideicon)
                    + mPopMenuGroup.mPopTag)) {
                applistGroup.hideIcon();
            } else if (nameTag.equals(context.getResources().getString(R.string.zen_settings)
                    + mPopMenuGroup.mPopTag)) {
                applistGroup.ZenSettings();
            }
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }

        }
        else {
            int tabNum = 0;
            if (nameTag.equals(util.tabName[0])) {
                tabViewGroup.changeTabLeft(0);
                tabNum = 0;
            } else if (nameTag.equals(util.tabName[1])) {
                tabViewGroup.changeTabLeft(1);
                tabNum = 1;
            } else if (nameTag.equals(util.tabName[2])) {
                tabViewGroup.changeTabLeft(2);
                tabNum = 2;
            } else if (nameTag.equals(util.tabName[3])) {
                tabViewGroup.changeTabLeft(3);
                tabNum = 3;
            } else if (nameTag.equals(util.tabName[4])) {
                tabViewGroup.changeTabLeft(4);
                tabNum = 4;
            } else if (nameTag.equals(util.tabName[5])) {
                tabViewGroup.changeTabLeft(5);
                tabNum = 5;
            }
            util.setTabNum(tabNum);
            if (util.getPreferences() != null) {
                util.getPreferences().edit().putInt(util.gettabNumKey(), tabNum).commit();
            }

            titleBar.setTextName(nameTag);
            applistGroup.setTab(tabNum);
        }
    }

    private void doneSomethingInOption(View view) {
        if (mPopMenuGroup == null) {
            mPopMenuGroup = new PopMenuGroup(context, this);
        }
        if (mPopupWindow == null) {
            int popWidth = context.getResources().getDimensionPixelSize(R.dimen.popmenu_width);
            int popHeight = context.getResources().getDimensionPixelSize(R.dimen.popmenu_height);
            mPopupWindow = new PopupWindow(mPopMenuGroup, popWidth, popHeight);
            mPopupWindow.setBackgroundDrawable(new
                    ColorDrawable(Color.argb(255, 0, 0, 0)));
        }
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        int xoff = context.getResources().getDimensionPixelSize(R.dimen.popmenu_xoff);
        int yoff = context.getResources().getDimensionPixelSize(R.dimen.popmenu_yoff);
        mPopupWindow.showAsDropDown(view, xoff, yoff);

    }

}
