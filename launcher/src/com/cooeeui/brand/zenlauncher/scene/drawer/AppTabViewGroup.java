
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;

import com.cooeeui.brand.zenlauncher.R;

public class AppTabViewGroup extends MyRelativeLayout implements IAppGroup {

    private int oldWidth = groupWidth + 1;
    private int oldHeight = groupHeight + 1;
    private AppListUtil util = null;
    private ImageView tabImageView = null;
    private ClickButtonOnClickListener onClickListener = null;
    private int paddingTop = -1;
    private int paddingLeft = -1;
    private int tabButtonWidth = -1;
    private int oldNum = -1;
    private int[] tabIconId = new int[] {
            R.drawable.applayout_favourite,
            R.drawable.applayout_communication,
            R.drawable.applayout_life,
            R.drawable.applayout_soclal,
            R.drawable.applayout_system,
            R.drawable.applayout_tool
    };
    private int[] tabPotion = new int[tabIconId.length];
    private MyButton[] myButtons = new MyButton[tabIconId.length];

    public AppTabViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (oldWidth != groupWidth || oldHeight != groupHeight) {
            oldWidth = groupWidth;
            oldHeight = groupHeight;
            addTabButtonView();
        }
    }

    public AppListUtil getUtil() {
        return util;
    }

    public void setUtil(AppListUtil util) {
        this.util = util;
    }

    public ClickButtonOnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(ClickButtonOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * 添加底边的tab
     */
    private void addTabButtonView() {
        if (tabIconId.length == util.tabName.length) {
            int tabNum = tabIconId.length;
            tabButtonWidth = (int) (groupHeight * 0.85f);
            paddingTop = (int) ((groupHeight - tabButtonWidth) / 2f);
            paddingLeft = (int) ((groupWidth / tabNum - tabButtonWidth) / 2f);
            int imageWidth = (int) (util.getLineWidth() * util.getDensity());
            tabImageView.setY(groupHeight - imageWidth);
            tabImageView.setBackgroundResource(R.drawable.applayout_split_line);
            LayoutParams imageLp = new LayoutParams(tabButtonWidth, imageWidth);
            tabImageView.setLayoutParams(imageLp);
            for (int i = 0; i < myButtons.length; i++) {
                MyButton tabButton = myButtons[i];
                int px = paddingLeft * (2 * i + 1) + tabButtonWidth * i;
                tabButton.setX(px);
                tabPotion[i] = px + tabButtonWidth;
                tabButton.setY(paddingTop);
                LayoutParams lp = new LayoutParams(tabButtonWidth, tabButtonWidth);
                tabButton.setLayoutParams(lp);
            }
            changeTabLeft(util.getTabNum());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        Log.v("", "AppTabViewGroup onTouchEvent x is " + x + " action is " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (tabImageView != null) {
                    tabImageView.setX(x);
                    invalidate();
                    changeValueByX(x);
                }

                break;
            case MotionEvent.ACTION_UP:
                changeValueByX(x);
                changeTabLeft(oldNum);
                break;
            default:
                break;
        }
        return true;
    }

    private void changeValueByX(int x) {
        int num = getNumByX(x);
        Log.v("", "whj num is " + num);
        if (oldNum != num && num >= 0 && num < util.tabName.length) {
            oldNum = num;
            String value = util.tabName[num];
            onClickListener.doneChangeByValue(value, null);
        }
    }

    private int getNumByX(int x) {
        if (x < tabPotion[0]) {
            return 0;
        } else {
            for (int i = 1; i < tabPotion.length; i++) {
                if (x < tabPotion[i] && x >= tabPotion[i - 1]) {
                    return i;
                }
            }
        }
        return 0;
    }

    public void changeTabLeft(int num) {
        if (tabImageView != null && num < tabPotion.length) {
            int x = tabPotion[num] - tabButtonWidth;
            tabImageView.setX(x);
        }
    }

    @Override
    public void initViewData() {
        tabImageView = new ImageView(getContext());
        this.addView(tabImageView);
        for (int i = 0; i < myButtons.length; i++) {
            MyButton tabButton = new MyButton(getContext());
            tabButton.setOnClickListener(onClickListener);
            tabButton.setTag(util.tabName[i]);
            tabButton.setBackgroundResource(tabIconId[i]);
            myButtons[i] = tabButton;
            this.addView(tabButton);
        }
    }

    private class MyButton extends Button {

        public MyButton(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean ret = super.onTouchEvent(event);
            Log.e("", "MyButton onTouchEvent action is " + event.getAction() + " ret is " + ret);
            return false;
        }
    }

}
