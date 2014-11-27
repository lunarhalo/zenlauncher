
package com.cooeeui.brand.zenlauncher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class DragController {

    public static int DRAG_ACTION_MOVE = 0;

    private Launcher mLauncher;
    private Handler mHandler;

    private boolean mDragging;

    private int mMotionDownX;

    private int mMotionDownY;

    private DragView mDragObject;

    interface DragListener {

        void onDragStart();

        void onDragEnd();
    }

    public DragController(Launcher launcher) {
        mLauncher = launcher;
        mHandler = new Handler();
    }

    public boolean dragging() {
        return mDragging;
    }

    private Bitmap makeDefaultIcon() {
        LauncherAppState app = LauncherAppState.getInstance();
        Drawable d = app.getIconCache().getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    public void startDrag() {
        Log.e("launcher123", "onlongclick dragcontroller");
        Bitmap b = makeDefaultIcon();
        mDragging = true;
        mDragObject = new DragView(mLauncher, b, 0,
                0, 0, 0, b.getWidth(), b.getHeight(), 0);

        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        mDragObject.show(mMotionDownX, mMotionDownY);
        // handleMoveEvent(mMotionDownX, mMotionDownY);
    }

    Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    public boolean isDragging() {
        return mDragging;
    }

    public void cancelDrag() {

        endDrag();
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
        }
    }

    private int[] getClampedDragLayerPos(float x, float y) {
        int mTmpPoint[] = new int[2];
        Rect mDragLayerRect = new Rect();
        mLauncher.getDragLayer().getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;

                break;
            case MotionEvent.ACTION_UP:
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return mDragging;
    }

    private void handleMoveEvent(int x, int y) {
        mDragObject.move(x, y);

    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;

                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_UP:
                // Ensure that we've processed a move event at the current
                // pointer location.
                handleMoveEvent(dragLayerX, dragLayerY);

                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return true;
    }

    DragView getDragView() {
        return mDragObject;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

}
