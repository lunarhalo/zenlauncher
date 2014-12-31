
package com.cooeeui.brand.zenlauncher.scenes.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.managers.BitmapManager;
import com.cooeeui.brand.zenlauncher.utils.DensityUtil;

public class IconLayout extends FrameLayout {
    Rect mRect = new Rect();
    Rect mImageRect = new Rect();
    Paint mPaint = new Paint();
    Matrix mMatrix = new Matrix();
    int mState = 0;

    public IconLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        View v = findViewById(R.id.icon_image);
        getGlobalVisibleRect(mRect);
        v.getGlobalVisibleRect(mImageRect);
        mImageRect.offset(-mRect.left, -mRect.top);

        // show subscript
        if (mState != 0) {
            Bitmap bitmap = null;

            if (mState == 1)
                bitmap = BitmapManager.getInstance().get("app_hide");
            else if (mState == 2)
                bitmap = BitmapManager.getInstance().get("app_uninstall");

            // get scale
            float scale = 1.0f;
            float width = mRect.width() * 0.25f;
            scale = width / bitmap.getWidth();
            // Log.v("suyu",
            // "mRect.width() = " + mRect.width() + ", hide.getWidth() = " +
            // bitmap.getWidth()
            // + ", width = " + width + ", scale = "
            // + scale);

            // get translation position
            float x = mImageRect.left;
            float y = mImageRect.top;
            x += mImageRect.width() - width / 2;
            y += -width / 2;
            // Log.v("suyu", "x = " + x + ", y = " + y);

            // contract a little
            x -= DensityUtil.dip2px(getContext(), 2);
            y += DensityUtil.dip2px(getContext(), 2);

            mMatrix.reset();
            mMatrix.setScale(scale, scale);
            mMatrix.postTranslate(x, y);

            canvas.drawBitmap(bitmap, mMatrix, mPaint);
        }
    }

    void setState(int state) {
        if (mState != state) {
            mState = state;

            invalidate();
        }
    }

    public void entryUninstallState() {
        setState(2);
    }

    public void entryHideState() {
        setState(1);
    }

    public void entryNormalState() {
        setState(0);
    }
}
