
package com.cooeeui.brand.zenlauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cooeeui.brand.zenlauncher.apps.IconCache;

public class Workspace extends ViewGroup {

    private Launcher mLauncher;
    private IconCache mIconCache;
    private DragController mDragController;

    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = (Launcher) context;

        Button btn = new Button(context);
        btn.setText("hello");
        btn.setTranslationX(20);
        btn.setTranslationY(80);
        addView(btn);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        // int childCount = getChildCount();
        //
        // if (childCount == 0) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // return;
        // }
        //
        // int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        // int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //
        // if (widthMode == MeasureSpec.UNSPECIFIED || heightMode ==
        // MeasureSpec.UNSPECIFIED) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // return;
        // }
        //
        // // Return early if we aren't given a proper dimension
        // if (widthSize <= 0 || heightSize <= 0) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // return;
        // }
        //
        // for (int i = 0; i < childCount; i++) {
        // View v = getChildAt(i);
        // if (v.getVisibility() != GONE) {
        // final LayoutParams lp = (LayoutParams) v.getLayoutParams();
        // int childWidthMode;
        // int childHeightMode;
        //
        // if (lp.width == LayoutParams.WRAP_CONTENT) {
        // childWidthMode = MeasureSpec.AT_MOST;
        // } else {
        // childWidthMode = MeasureSpec.EXACTLY;
        // }
        //
        // if (lp.height == LayoutParams.WRAP_CONTENT) {
        // childHeightMode = MeasureSpec.AT_MOST;
        // } else {
        // childHeightMode = MeasureSpec.EXACTLY;
        // }
        //
        // final int childWidthMeasureSpec =
        // MeasureSpec.makeMeasureSpec(widthSize, childWidthMode);
        // final int childHeightMeasureSpec =
        // MeasureSpec.makeMeasureSpec(heightSize, childHeightMode);
        // v.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        // }
        // setMeasuredDimension(widthSize, heightSize);
        //
        // Log.e("launcher123", "onMeasure workspace heightSize = " + heightSize
        // + " widthSize = "
        // + widthSize);
        // }

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub
        int childCount = getChildCount();

        Log.e("launcher123", "onLayout workspace - l = " + left + " t = " + top);
        Log.e("launcher123", "onLayout workspace - r = " + right + " b = " + bottom);
        Log.e("launcher123", "workspace onLayout childCount = " + childCount);

        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            // v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            v.layout((int) v.getTranslationX(), (int) v.getTranslationY(),
                    (int) v.getTranslationX() + (int) v.getMeasuredWidth(),
                    (int) v.getTranslationY() + (int) v.getMeasuredHeight());

            // v.layout(left, top, right, bottom);
            Log.e("launcher123", "onLayout workspace l = " + v.getLeft() + " t = " + v.getTop());
            Log.e("launcher123", "onLayout workspace r = " + v.getRight() + " b = " + v.getBottom());
            Log.e("launcher123",
                    "onLayout workspace w = " + v.getMeasuredWidth() + " h = "
                            + v.getMeasuredHeight());
            Log.e("launcher123",
                    "onLayout workspace x = " + v.getTranslationX() + " y = " + v.getTranslationY());
            Log.e("launcher123",
                    "onLayout workspace sx = " + v.getScrollX() + " sy = " + v.getScrollY());

        }
    }

    void setup(DragController dragController) {
        mDragController = dragController;
    }

    void startDrag() {
        Log.e("launcher123", "onlongclick workspace");
        mDragController.startDrag();
    }
}
