
package com.cooeeui.brand.zenlauncher.scenes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.scenes.ui.BubbleView;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragSource;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget.DragObject;

public class Workspace extends FrameLayout implements DragSource {

    private Launcher mLauncher;
    private IconCache mIconCache;
    private DragController mDragController;

    private int mIconSize;
    private float mPadding;
    private float[] mMidPoint = new float[2];

    public ArrayList<BubbleView> mBubbleViews = new ArrayList<BubbleView>();

    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Workspace(Context context) {
        super(context);
    }

    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
        LauncherAppState app = LauncherAppState.getInstance();
        mIconCache = app.getIconCache();

        init();
    }

    public void addBubbleViewFromBind(ShortcutInfo info) {
        BubbleView v = new BubbleView(mLauncher, info.getIcon(mIconCache));
        v.setTag(info);
        addView(v);
        mBubbleViews.add(v);
        v.setOnClickListener(mLauncher);
        v.setOnLongClickListener(mLauncher);

        mDragController.addDropTarget(v);
    }

    public void startBind() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof BubbleView) {
                mDragController.removeDropTarget((DropTarget) v);
                BubbleView view = (BubbleView) v;
                view.clearBitmap();
            }
        }
        mBubbleViews.clear();
        removeAllViews();
    }

    public void finishBind() {
        Collections.sort(mBubbleViews, new Comparator<BubbleView>() {
            public int compare(BubbleView a, BubbleView b) {
                Object aTag = a.getTag();
                Object bTag = b.getTag();
                final ShortcutInfo aShortcut = (ShortcutInfo) aTag;
                final ShortcutInfo bShortcut = (ShortcutInfo) bTag;
                return aShortcut.position - bShortcut.position;
            }
        });
        update();
    }

    public void removeBubbleView(BubbleView view) {
        mBubbleViews.remove(view);
        removeView(view);
        update();

        mDragController.removeDropTarget(view);
    }

    private void moveBubbleView(int position, float x, float y) {
        BubbleView v = mBubbleViews.get(position);
        if (v != null) {
            v.move(x, y);
        }
    }

    void init() {
        DisplayMetrics metrics = new DisplayMetrics();
        mLauncher.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mIconSize = metrics.widthPixels * 96 / 720;
        mPadding = metrics.widthPixels * 36 / 720;
        mMidPoint[0] = metrics.widthPixels * 180 / 720;
        mMidPoint[1] = metrics.heightPixels - mIconSize * 4 - mPadding * 2;
    }

    public void update() {
        int count = mBubbleViews.size();

        float startX = mMidPoint[0];
        float startY = mMidPoint[1];
        if (count > 6) {
            startY += (mIconSize + mPadding);
        }
        float iconX, iconY;
        int rNum = count % 3;

        if (rNum == 1) {
            iconX = startX + mIconSize + mPadding;
            iconY = startY - (mIconSize + mPadding) * (count / 3);
            moveBubbleView(count - 1, iconX, iconY);
        } else if (rNum == 2) {
            iconX = startX + (mIconSize + mPadding) / 2;
            iconY = startY - (mIconSize + mPadding) * (count / 3);
            moveBubbleView(count - 2, iconX, iconY);
            moveBubbleView(count - 1, iconX + mIconSize + mPadding, iconY);
        }

        rNum = count - rNum;
        iconX = startX;
        iconY = startY;
        for (int i = 0; i < rNum; i++) {
            moveBubbleView(i, iconX, iconY);
            iconX += (mIconSize + mPadding);
            if ((i + 1) % 3 == 0) {
                iconX = startX;
                iconY -= (mIconSize + mPadding);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);
    }

    public void startDrag(BubbleView view) {
        removeView(view);
        mDragController.removeDropTarget(view);
        mDragController.startDrag(this, view);
    }

    @Override
    public void onDropCompleted(BubbleView target, DragObject d) {
        if (target != null) {
            int tIndex = mBubbleViews.indexOf(target);
            int sIndex = mBubbleViews.indexOf(d.dragView);
            mBubbleViews.set(tIndex, d.dragView);
            mBubbleViews.set(sIndex, target);
        }
        mLauncher.getDragLayer().removeView(d.dragView);
        mDragController.addDropTarget(d.dragView);
        addView(d.dragView);
        update();
    }
}
