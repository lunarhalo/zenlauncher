
package com.cooeeui.brand.zenlauncher.scenes.ui;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragLayer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class DragView extends View {

    private Bitmap mBitmap;

    private Paint mPaint;

    private DragLayer mDragLayer = null;

    public DragView(Launcher launcher, Bitmap bitmap, int registrationX, int registrationY,
            int left, int top, int width, int height, final float initialScale) {
        super(launcher);
        mDragLayer = launcher.getDragLayer();

        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);

        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
    }

    public void show(int touchX, int touchY) {
        mDragLayer.addView(this);

        DragLayer.LayoutParams lp = new DragLayer.LayoutParams(0, 0);
        lp.width = mBitmap.getWidth();
        lp.height = mBitmap.getHeight();
        setLayoutParams(lp);
        setTranslationX(touchX - mBitmap.getWidth() / 2);
        setTranslationY(touchY - mBitmap.getHeight() / 2);
    }

    void move(int touchX, int touchY) {
        setTranslationX(touchX - mBitmap.getWidth() / 2);
        setTranslationY(touchY - mBitmap.getHeight() / 2);
    }

    void remove() {
        if (getParent() != null) {
            mDragLayer.removeView(DragView.this);
        }
    }
}
