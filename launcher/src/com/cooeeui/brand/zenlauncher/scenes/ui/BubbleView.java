
package com.cooeeui.brand.zenlauncher.scenes.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;

public class BubbleView extends View implements DropTarget {

    private Bitmap mBitmap;

    private Paint mPaint;

    private int mSize;
    private int mPadding;

    private float x;
    private float y;

    Matrix mMatrix;
    private Launcher mLauncher;

    private static final int DRAG_ENTER_VIEW = 1;
    private static final int DRAG_EXIT_VIEW = 2;

    private static final int DELAYED_TIME = 220;
    private Handler mHandler;

    private long mTime;

    private static final int ALPHA_DURATION = 100;
    private ValueAnimator mAlphaHide;
    private ValueAnimator mAlphaShow;
    private AnimatorSet mAnimatorSet;
    private int mWhat;

    public BubbleView(Context context, Bitmap bitmap) {
        this(context, bitmap, bitmap.getWidth());
        mLauncher = (Launcher) context;
    }

    public BubbleView(Context context, Bitmap bitmap, int size) {
        super(context);
        mBitmap = bitmap;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mMatrix = new Matrix();
        mSize = size;

        mAlphaHide = ValueAnimator.ofFloat(0.3f, 0);
        mAlphaHide.setDuration(ALPHA_DURATION);
        mAlphaHide.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                setAlpha(alpha);
            }
        });

        mAlphaHide.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mWhat == DRAG_ENTER_VIEW) {
                    setTranslationX(mLauncher.getSpeedDial().getSelectX());
                    setTranslationY(mLauncher.getSpeedDial().getSelectY());
                } else {
                    setTranslationX(x);
                    setTranslationY(y);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });

        mAlphaShow = ValueAnimator.ofFloat(0, 0.3f);
        mAlphaShow.setDuration(ALPHA_DURATION);
        mAlphaShow.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                setAlpha(alpha);
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.play(mAlphaHide).before(mAlphaShow);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DRAG_ENTER_VIEW:
                        mWhat = DRAG_ENTER_VIEW;
                        mAnimatorSet.start();
                        break;
                    case DRAG_EXIT_VIEW:
                        mWhat = DRAG_EXIT_VIEW;
                        mAnimatorSet.start();
                        break;
                }
            }
        };

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            float scale = (float) mSize / (float) mBitmap.getWidth();
            mMatrix.setScale(scale, scale);
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        }
    }

    public void setSize(int size, int padding) {
        mSize = size;
        mPadding = padding;
    }

    public void move(float x, float y) {
        mHandler.removeMessages(DRAG_ENTER_VIEW);
        mHandler.removeMessages(DRAG_EXIT_VIEW);
        mAnimatorSet.end();
        setTranslationX(x);
        setTranslationY(y);
        this.x = x;
        this.y = y;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    public void onDrop(DragObject dragObject) {
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        mHandler.removeMessages(DRAG_ENTER_VIEW);
        mHandler.removeMessages(DRAG_EXIT_VIEW);
        mHandler.sendEmptyMessageDelayed(DRAG_ENTER_VIEW, DELAYED_TIME);

        mTime = System.currentTimeMillis();
    }

    @Override
    public void onDragOver(DragObject dragObject) {
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        long diff = System.currentTimeMillis() - mTime;
        if (diff < DELAYED_TIME) {
            mHandler.removeMessages(DRAG_ENTER_VIEW);
            return;
        }

        mHandler.removeMessages(DRAG_EXIT_VIEW);
        mHandler.sendEmptyMessageDelayed(DRAG_EXIT_VIEW, DELAYED_TIME);
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        outRect.left = (int) x - mPadding / 2;
        outRect.top = (int) y - mPadding / 2;
        outRect.right = outRect.left + mSize + mPadding;
        outRect.bottom = outRect.top + mSize + mPadding;
        if (outRect.left < 0) {
            outRect.left = 0;
        }
        if (outRect.top < 0) {
            outRect.top = 0;
        }
    }

    public void changeBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    public void clearBitmap() {
        if (mBitmap != null) {
            if (!mBitmap.isRecycled())
                mBitmap.recycle();
            mBitmap = null;
        }
    }
}
