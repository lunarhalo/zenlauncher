
package com.cooeeui.brand.zenlauncher.scenes.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;

public class BubbleView extends View implements DropTarget {

    private Bitmap mBitmap;

    private Paint mPaint;

    private int mWidth;

    private int mHeight;

    public BubbleView(Context context, Bitmap bitmap) {
        super(context);
        mBitmap = bitmap;
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
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
        outRect.right = outRect.left + mWidth;
        outRect.bottom = outRect.top + mHeight;
    }

    public void clearBitmap() {
        mBitmap.recycle();
    }
}
