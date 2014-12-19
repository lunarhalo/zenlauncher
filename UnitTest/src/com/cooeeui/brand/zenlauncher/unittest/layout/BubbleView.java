
package com.cooeeui.brand.zenlauncher.unittest.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

public class BubbleView extends View {

    private Bitmap mBitmap;

    private Paint mPaint;

    private int mSize;

    Matrix mMatrix;

    public BubbleView(Context context, Bitmap bitmap) {
        super(context);
        mBitmap = bitmap;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mMatrix = new Matrix();
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        if (widthSize > WorkspaceLayout.ICON_SIZE_MAX) {
            setMeasuredDimension(mSize, mSize);
            return;
        } else {
            mSize = widthSize;
            setMeasuredDimension(mSize, mSize);
        }
    }

    public void move(float x, float y) {
        setTranslationX(x);
        setTranslationY(y);
    }

}
