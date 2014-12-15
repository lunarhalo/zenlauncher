
package com.cooeeui.brand.zenlauncher.unittest.layout;

import com.cooeeui.brand.zenlauncher.unittest.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class FitCenterLayout extends ViewGroup {
    float mChildAspectRatio = 1.0f;
    Rect mRect = new Rect();
    Rect mChildRect = new Rect();
    float mWidthWeight = 1.0f;
    float mHeightWeight = 1.0f;

    public FitCenterLayout(Context context) {
        super(context);
    }

    public FitCenterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get the custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.zenlauncher);
        int indexCount = a.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = a.getIndex(i);
            switch (index) {
                case R.styleable.zenlauncher_aspect_ratio:
                    mChildAspectRatio = a.getFloat(index, 1);
                    break;
                case R.styleable.zenlauncher_width_weight:
                    mWidthWeight = a.getFloat(index, 1);
                    break;
                case R.styleable.zenlauncher_height_weight:
                    mHeightWeight = a.getFloat(index, 1);
                    break;
            }
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // calculate rectangle of child
        mRect.set(l, t, r, b);
        applyAspectRatio(mChildAspectRatio);

        // layout child
        View v = getChildAt(0);
        if (v != null) {
            v.layout(mChildRect.left, mChildRect.top, mChildRect.right, mChildRect.bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Apply child aspect ratio, save the result in mChildRect.
     */
    void applyAspectRatio(float aspect) {
        mChildRect.set(mRect);

        // guard
        if (mRect.height() <= 0 || aspect == 0.0f) {
            return;
        }

        float srcAspect = mRect.width() / mRect.height();
        if (srcAspect < aspect) {
            int width = (int) (mRect.width() * mWidthWeight);
            int height = (int) (width / aspect);
            mChildRect.set(mRect.centerX() - width / 2,
                    mRect.centerY() - height / 2,
                    mRect.centerX() + width / 2,
                    mRect.centerY() + height / 2);
        } else if (srcAspect > aspect) {
            int height = (int) (mRect.height() * mHeightWeight);
            int width = (int) (height * aspect);
            mChildRect.set(mRect.centerX() - width / 2,
                    mRect.centerY() - height / 2,
                    mRect.centerX() + width / 2,
                    mRect.centerY() + height / 2);
        }
    }
}
