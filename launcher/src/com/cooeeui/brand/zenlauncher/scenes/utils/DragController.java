
package com.cooeeui.brand.zenlauncher.scenes.utils;

import java.util.ArrayList;

import android.graphics.Rect;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.scenes.ui.BubbleView;

public class DragController {
    public static boolean isUp = false;

    public static int DRAG_ACTION_MOVE = 0;

    private Launcher mLauncher;

    private boolean mDragging;

    private int mMotionDownX;

    private int mMotionDownY;

    private int mOffsetX;

    private int mOffsetY;

    private int mMidOffsetX;

    private int mMidOffsetY;

    private Rect mRectTemp = new Rect();

    private DropTarget.DragObject mDragObject;

    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();

    private DropTarget mLastDropTarget;

    public DragController(Launcher launcher) {
        mLauncher = launcher;
    }

    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }

    public void startDrag(DragSource source, BubbleView view) {
        // get a offset rectangle of workspace.
        Rect r = new Rect();
        mLauncher.getSpeedDial().getGlobalVisibleRect(r);
        Rect rootRect = new Rect();
        mLauncher.getDragLayer().getGlobalVisibleRect(rootRect);
        r.offset(-rootRect.left, -rootRect.top);

        mDragging = true;
        mDragObject = new DropTarget.DragObject();
        mDragObject.dragView = view;
        mLauncher.getDragLayer().addView(view);
        mDragObject.dragSource = source;
        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        mOffsetX = mMotionDownX - (int) view.getTranslationX()
                - r.left;
        mOffsetY = mMotionDownY - (int) view.getTranslationY()
                - r.top;
        mMidOffsetX = mOffsetX - view.getWidth() / 2
                + r.left;
        mMidOffsetY = mOffsetY - view.getHeight() / 2
                + r.top;
        mDragObject.dragView.move(mMotionDownX - mOffsetX, mMotionDownY - mOffsetY);
    }

    public void startDrag(DragSource source, BubbleView view, int width) {

        mDragging = true;
        mDragObject = new DropTarget.DragObject();
        mDragObject.dragView = view;
        mLauncher.getDragLayer().addView(view);
        mDragObject.dragSource = source;
        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        mOffsetX = width / 2;
        mOffsetY = width / 2;
        mMidOffsetX = 0;
        mMidOffsetY = 0;

        mDragObject.dragView.move(mMotionDownX - mOffsetX, mMotionDownY - mOffsetY);

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
            mDragObject.dragSource.onDropCompleted(null);
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
            if (r.contains(x - mMidOffsetX, y - mMidOffsetY)) {
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
        mDragObject.dragSource.onDropCompleted((View) dropTarget);
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
        mDragObject.dragView.move(x - mOffsetX, y - mOffsetY);

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
                isUp = false;
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_UP:
                // Ensure that we've processed a move event at the current
                // pointer location.
                isUp = true;
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
