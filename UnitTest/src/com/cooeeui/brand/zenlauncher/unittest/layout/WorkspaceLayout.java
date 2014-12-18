
package com.cooeeui.brand.zenlauncher.unittest.layout;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class WorkspaceLayout extends FrameLayout {
    private Context mContext;

    public static final int ICON_SIZE_MAX = 144;
    public static final int ICON_SIZE_MIN = 10;
    private int mIconSize;
    private int mPadding;
    private float[] mMidPoint = new float[2];
    private int mWidth;
    private int mHeight;

    private static final int BUBBLE_VIEW_CAPACITY = 9;
    private ArrayList<BubbleView> mBubbleViews = new ArrayList<BubbleView>(BUBBLE_VIEW_CAPACITY);

    private Bitmap mDefaultIcon;

    public WorkspaceLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setup();
    }

    public WorkspaceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setup();
    }

    public WorkspaceLayout(Context context) {
        super(context);
        mContext = context;
        setup();
    }

    public void setup() {
        BitmapUtils.setIconSize(ICON_SIZE_MAX);
        for (int i = 0; i < BUBBLE_VIEW_CAPACITY; i++) {
            addBubbleView();
        }
    }

    private void addBubbleView() {
        BubbleView v = new BubbleView(mContext, getDefaultIcon());
        addView(v);
        mBubbleViews.add(v);
    }

    private Bitmap getDefaultIcon() {
        if (mDefaultIcon == null) {
            mDefaultIcon = BitmapUtils.getIcon(Resources.getSystem(),
                    android.R.mipmap.sym_def_app_icon);
        }
        return mDefaultIcon;
    }

    private void moveBubbleView(int position, float x, float y) {
        BubbleView v = mBubbleViews.get(position);
        if (v != null) {
            v.move(x, y);
        }
    }

    void initSize() {
        int length;
        if (mWidth > mHeight) {
            mMidPoint[0] = (mWidth - mHeight) / 2;
            mMidPoint[1] = 0;
            length = mHeight;

        } else {
            mMidPoint[0] = 0;
            mMidPoint[1] = (mHeight - mWidth) / 2;
            length = mWidth;
        }
        mPadding = length / 10;
        mIconSize = (length - mPadding * 2) / 3;
        mMidPoint[1] += (mIconSize + mPadding);
        if (mIconSize > ICON_SIZE_MAX) {
            int offset = mIconSize - ICON_SIZE_MAX;
            mPadding += offset;
            mMidPoint[0] += offset / 2;
            mMidPoint[1] += offset / 2;
            mIconSize = ICON_SIZE_MAX;
        } else if (mIconSize < ICON_SIZE_MIN) {
            mIconSize = ICON_SIZE_MIN;
        }

        BitmapUtils.setIconSize(mIconSize);
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        update();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mWidth != widthSize || mHeight != heightSize) {
            mWidth = widthSize;
            mHeight = heightSize;
            initSize();
        }

        int sizeMeasureSpec = MeasureSpec.makeMeasureSpec(mIconSize, MeasureSpec.EXACTLY);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.measure(sizeMeasureSpec, sizeMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

}
