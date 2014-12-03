
package com.cooeeui.brand.zenlauncher.scenes.utils;

import com.cooeeui.brand.zenlauncher.Launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

public class DragLayer extends FrameLayout {
    private DragController mDragController;

    private Launcher mLauncher;

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragController.onInterceptTouchEvent(ev);
    }

    // @Override
    // public boolean onInterceptHoverEvent(MotionEvent ev) {
    //
    // return false;
    // }

    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        return super.onRequestSendAccessibilityEvent(child, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDragController.onTouchEvent(ev);
    }

    // @Override
    // public boolean dispatchUnhandledMove(View focused, int direction) {
    // // return mDragController.dispatchUnhandledMove(focused, direction);
    // return super.dispatchUnhandledMove(focused, direction);
    // }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }
}
