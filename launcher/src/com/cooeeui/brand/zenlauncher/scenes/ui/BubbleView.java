
package com.cooeeui.brand.zenlauncher.scenes.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.cooeeui.brand.zenlauncher.scenes.SpeedDial;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;

public class BubbleView extends View implements DropTarget {

    private Bitmap mBitmap;

    private Paint mPaint;

    private int mSize;

    Matrix mMatrix;

    public BubbleView(Context context, Bitmap bitmap) {
        this(context, bitmap, bitmap.getWidth());
    }

    public BubbleView(Context context, Bitmap bitmap, int size) {
        super(context);
        mBitmap = bitmap;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mMatrix = new Matrix();
        mSize = size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            float scale = (float) mSize / (float) mBitmap.getWidth();
            mMatrix.setScale(scale, scale);
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        }
    }

    public void setSize(int size) {
        if (size != mSize) {
            mSize = size;
            invalidate();
        }
    }

    public void move(float x, float y) {
        setTranslationX(x);
        setTranslationY(y);
    }

    @Override
    public void onDrop(DragObject dragObject) {
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
    }

    @Override
    public void onDragOver(DragObject dragObject) {
    }

    @Override
    public void onDragExit(DragObject dragObject) {
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        outRect.left = (int) getTranslationX();
        outRect.top = (int) getTranslationY();
        outRect.right = outRect.left + mSize;
        outRect.bottom = outRect.top + mSize;
    }

    public void changeBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    public void clearBitmap() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
