
package com.cooeeui.brand.zenlauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class BubbleView extends View implements DropTarget {

    private Bitmap mBitmap;

    private Paint mPaint;

    private int mWidth;

    private int mHeight;

    public BubbleView(Context context, Bitmap bitmap) {
        super(context);
        // TODO Auto-generated constructor stub
        mBitmap = bitmap;
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        setMeasuredDimension(mWidth, mHeight);
    }

    void move(float x, float y) {
        setTranslationX(x);
        setTranslationY(y);
    }

    @Override
    public void onDrop(DragObject dragObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragOver(DragObject dragObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragExit(DragObject dragObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        // TODO Auto-generated method stub
        outRect.left = (int) getTranslationX();
        outRect.top = (int) getTranslationY();
        outRect.right = outRect.left + mWidth;
        outRect.bottom = outRect.top + mHeight;
    }

}
