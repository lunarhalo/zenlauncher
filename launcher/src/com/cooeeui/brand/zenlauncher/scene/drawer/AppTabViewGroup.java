
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;

public class AppTabViewGroup extends MyRelativeLayout implements IAppGroup {

    private int oldWidth = groupWidth + 1;
    private int oldHeight = groupHeight + 1;
    private AppListUtil util = null;
    private ImageView tabImageView = null;
    private ClickButtonOnClickListener onClickListener = null;
    private DragController mDragController = null;
    private int paddingTop = -1;
    private int paddingLeft = -1;
    private int tabButtonWidth = -1;
    private int oldNum = -1;
    /**
     * 用于在DragExit时候原先旧的GridView和现在新的GridView的一个比较
     */
    private int mDragExitOldnum = -1;

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
    private boolean mIsAppTabDragging = false;

    public DragController getmDragController() {
        return mDragController;
    }

    public void setmDragController(DragController mDragController) {
        this.mDragController = mDragController;
    }

    // private int oldNum = -1;
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
            oldNum = util.getTabNum();
            setTabX(oldNum);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsAppTabDragging) {
            return true;
        }
        int x = (int) event.getX();
        int startX = (int) tabImageView.getX();
        changeTabNumByX(x);
        int endX = getTabPotion(getNumByX(x));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (util.ismIsTabViewAnimDone()) {
                    startViewTranslateAnim(startX, endX, tabImageView);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (tabImageView != null) {
                    tabImageView.setX(x);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (util.ismIsTabViewAnimDone()) {
                    startViewTranslateAnim(startX, endX, tabImageView);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void startViewTranslateAnim(int startX, int endX, final View view) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startX, endX);
        valueAnimator.setDuration(200);
        valueAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                util.setmIsTabViewAnimDone(false);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                util.setmIsTabViewAnimDone(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int valueX = (Integer) animation.getAnimatedValue();
                view.setTranslationX(valueX);
            }
        });
        valueAnimator.start();
    }

    private void changeTabNumByX(int x) {
        int num = getNumByX(x);
        // Log.v("", "whj num is " + num + " x is " + x);
        if (oldNum != num && num >= 0 && num < util.tabName.length) {
            oldNum = num;
            onClickListener.changeTabByNum(num);
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

    private int getNumByTag(String tagName) {
        int num = 0;
        for (int i = 0; i < util.tabName.length; i++) {
            if (util.tabName[i].equals(tagName)) {
                return i;
            }
        }
        return num;
    }

    private void setTabX(int num) {
        if (tabImageView != null) {
            int px = getTabPotion(num);
            tabImageView.setX(px);
        }
    }

    private int getTabPotion(int num) {
        int x = 0;
        if (num < tabPotion.length) {
            x = tabPotion[num] - tabButtonWidth;
        }
        return x;
    }

    @Override
    public void initViewData() {
        tabImageView = new ImageView(getContext());
        this.addView(tabImageView);
        for (int i = 0; i < myButtons.length; i++) {
            MyButton tabButton = new MyButton(getContext());
            // tabButton.setOnClickListener(onClickListener);
            tabButton.setTag(util.tabName[i]);
            tabButton.setBackgroundResource(tabIconId[i]);
            mDragController.addDropTarget(tabButton);
            myButtons[i] = tabButton;
            this.addView(tabButton);
        }
    }

    private class MyButton extends Button implements DropTarget {

        public MyButton(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean ret = super.onTouchEvent(event);
            Log.e("", "MyButton onTouchEvent action is " + event.getAction() + " ret is " + ret);
            return false;
        }

        @Override
        public void onDrop(DragObject dragObject) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDragEnter(DragObject dragObject) {
            mIsAppTabDragging = true;
            int tabNum = getNumByTag((String) (this.getTag()));
            if (oldNum != tabNum) {
                onClickListener.changeTabByNum(tabNum);
                int endX = getTabPotion(tabNum);
                tabImageView.setTranslationX(endX);
                // int startX = getTabPotion(oldNum);
                // if (util.ismIsTabViewAnimDone()) {
                // startViewTranslateAnim(startX, endX, tabImageView);
                // }
                oldNum = tabNum;
            }
        }

        @Override
        public void onDragOver(DragObject dragObject) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDragExit(DragObject dragObject) {
            if (DragController.isUp) {
                int tabNum = getNumByTag((String) (this.getTag()));
                if (mDragExitOldnum != tabNum) {
                    onClickListener.getApplistGroup().changeTabNum(mDragExitOldnum, tabNum);
                    mDragExitOldnum = tabNum;
                }
            }
            mIsAppTabDragging = false;
        }

        @Override
        public void getHitRectRelativeToDragLayer(Rect outRect) {
            outRect.left = (int) this.getX();
            outRect.top = this.getTop() + AppTabViewGroup.this.getTop();
            outRect.right = (int) this.getX() + this.getWidth();
            outRect.bottom = this.getBottom() + AppTabViewGroup.this.getTop();
        }
    }

    public void startDrag() {
        mDragExitOldnum = oldNum;
    }

}
