
package com.cooeeui.brand.zenlauncher;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class DragController {

    public static int DRAG_ACTION_MOVE = 0;

    private Launcher mLauncher;
    private Handler mHandler;

    private boolean mDragging;

    private int mMotionDownX;

    private int mMotionDownY;

    private Rect mRectTemp = new Rect();

    private DropTarget.DragObject mDragObject;

    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();

    private DropTarget mLastDropTarget;

    public DragController(Launcher launcher) {
        mLauncher = launcher;
        mHandler = new Handler();
    }

    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
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

    public void startDrag(DragSource source, BubbleView view) {
        Bitmap b = makeDefaultIcon();
        mDragging = true;
        mDragObject = new DropTarget.DragObject();
        mDragObject.dragView = view;
        mLauncher.getDragLayer().addView(view);
        mDragObject.dragSource = source;
        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        mDragObject.dragView.move(mMotionDownX, mMotionDownY);
    }

    public boolean isDragging() {
        return mDragging;
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
        }
    }

    public void cancelDrag() {
        if (mDragging) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.dragSource.onDropCompleted(null, mDragObject);
        }
        endDrag();
    }

    private DropTarget findDropTarget(int x, int y) {
        final Rect r = mRectTemp;

        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i = 0; i < count; i++) {
            DropTarget target = dropTargets.get(i);

            target.getHitRectRelativeToDragLayer(r);

            if (r.contains(x, y)) {
                return target;
            }
        }
        return null;
    }

    private void drop(int x, int y) {
        final DropTarget dropTarget = findDropTarget(x, y);

        if (dropTarget != null) {
            dropTarget.onDragExit(mDragObject);
            dropTarget.onDrop(mDragObject);
        }
        mDragObject.dragSource.onDropCompleted((BubbleView) dropTarget, mDragObject);
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
            case MotionEvent.ACTION_DOWN:
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                mLastDropTarget = null;
                break;
            case MotionEvent.ACTION_UP:
                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return mDragging;
    }

    private void checkTouchMove(DropTarget dropTarget) {
        if (dropTarget != null) {
            if (mLastDropTarget != dropTarget) {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragObject);
                }
                dropTarget.onDragEnter(mDragObject);
            }
            dropTarget.onDragOver(mDragObject);
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
        }
        mLastDropTarget = dropTarget;
    }

    private void handleMoveEvent(int x, int y) {
        mDragObject.dragView.move(x, y);

        DropTarget dropTarget = findDropTarget(x, y);
        checkTouchMove(dropTarget);

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
                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

}
