
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ClickButtonOnClickListener implements OnClickListener {

    private Context context = null;
    private AppNameViewGroup nameViewGroup = null;
    private AppListViewGroup applistGroup = null;
    private AppTabViewGroup tabViewGroup = null;
    private AppListUtil util = null;

    public AppListViewGroup getApplistGroup() {
        return applistGroup;
    }

    public void setApplistGroup(AppListViewGroup applistGroup) {
        this.applistGroup = applistGroup;
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

    public ClickButtonOnClickListener(Context context, AppListUtil util) {
        this.context = context;
        this.util = util;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        Object tag = v.getTag();
        if (tag instanceof String) {

            String nameTag = (String) tag;
            Log.v("", "whjtest onClick " + nameTag);
            if (nameTag.equals(util.optionName)) {
                doneSomethingInOption();
            } else {
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
                nameViewGroup.setTextName(nameTag);
                applistGroup.changeTextView(nameTag);
            }

        }
    }

    private void doneSomethingInOption() {
        // TODO Auto-generated method stub
        Toast.makeText(context, "Option", Toast.LENGTH_SHORT).show();
    }

}
