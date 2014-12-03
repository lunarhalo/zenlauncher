
package com.cooeeui.brand.zenlauncher;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.cooeeui.brand.zenlauncher.DropTarget.DragObject;
import com.cooeeui.brand.zenlauncher.apps.IconCache;

public class Workspace extends FrameLayout implements DragSource {

    private Launcher mLauncher;
    private IconCache mIconCache;
    private DragController mDragController;

    private int mIconSize;
    private float mPadding;
    private float[] mMidPoint = new float[2];

    private int[] icons = {
            R.drawable.icon1,
            R.drawable.icon2,
            R.drawable.icon3,
            R.drawable.icon4,
            R.drawable.icon5,
            R.drawable.icon6,
            R.drawable.icon7,
            R.drawable.icon8,
            R.drawable.icon9,
    };

    ArrayList<BubbleView> mApps = new ArrayList<BubbleView>();
    BubbleView mSelected;

    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public Workspace(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
        LauncherAppState app = LauncherAppState.getInstance();
        mIconCache = app.getIconCache();

        init();
    }

    Bitmap resizeBitmap(Bitmap bm, int width, int height)
    {
        Bitmap tmp = Bitmap.createScaledBitmap(bm, width, height, true);
        bm.recycle();
        return tmp;
    }

    private Bitmap makeIcon(int count) {
        Drawable d = mIconCache.getFullResIcon(mLauncher.getResources(), icons[count]);
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    private void addIcon(int count, float x, float y) {
        Bitmap b = makeIcon(count - 1);
        if (mIconSize != b.getWidth()) {
            b = resizeBitmap(b, mIconSize, mIconSize);
        }
        BubbleView v = new BubbleView(mLauncher, b);
        addView(v);
        mApps.add(v);
        v.move(x, y);
        v.setOnLongClickListener(mLauncher);

        mDragController.addDropTarget(v);
    }

    void removeIcon(BubbleView view) {
        mApps.remove(view);
        removeView(view);
        update();

        mDragController.removeDropTarget(view);
    }

    private void moveIcon(int count, float x, float y) {
        BubbleView v = mApps.get(count - 1);
        v.move(x, y);
    }

    void init() {
        DisplayMetrics metrics = new DisplayMetrics();
        mLauncher.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mIconSize = metrics.widthPixels * 96 / 720;
        mPadding = metrics.widthPixels * 36 / 720;
        mMidPoint[0] = metrics.widthPixels * 180 / 720;
        mMidPoint[1] = metrics.heightPixels - mIconSize * 4 - mPadding * 2;

        int count = 9;

        float startX = mMidPoint[0];
        float startY = mMidPoint[1];
        if (count > 6) {
            startY += (mIconSize + mPadding);
        }
        float iconX, iconY;
        int rNum = count % 3;

        if (rNum == 1) {
            iconX = startX + mIconSize + mPadding;
            iconY = startY - (mIconSize + mPadding) * (count / 3);
            addIcon(count, iconX, iconY);
        } else if (rNum == 2) {
            iconX = startX + (mIconSize + mPadding) / 2;
            iconY = startY - (mIconSize + mPadding) * (count / 3);
            addIcon(count - 1, iconX, iconY);
            addIcon(count, iconX + mIconSize + mPadding, iconY);
        }

        rNum = count - rNum;
        iconX = startX;
        iconY = startY;
        for (int i = 1; i <= rNum; i++) {
            addIcon(i, iconX, iconY);
            iconX += (mIconSize + mPadding);
            if (i % 3 == 0) {
                iconX = startX;
                iconY -= (mIconSize + mPadding);
            }
        }
    }

    void update() {
        int count = mApps.size();

        float startX = mMidPoint[0];
        float startY = mMidPoint[1];
        if (count > 6) {
            startY += (mIconSize + mPadding);
        }
        float iconX, iconY;
        int rNum = count % 3;

        if (rNum == 1) {
            iconX = startX + mIconSize + mPadding;
            iconY = startY - (mIconSize + mPadding) * (count / 3);
            moveIcon(count, iconX, iconY);
        } else if (rNum == 2) {
            iconX = startX + (mIconSize + mPadding) / 2;
            iconY = startY - (mIconSize + mPadding) * (count / 3);
            moveIcon(count - 1, iconX, iconY);
            moveIcon(count, iconX + mIconSize + mPadding, iconY);
        }

        rNum = count - rNum;
        iconX = startX;
        iconY = startY;
        for (int i = 1; i <= rNum; i++) {
            moveIcon(i, iconX, iconY);
            iconX += (mIconSize + mPadding);
            if (i % 3 == 0) {
                iconX = startX;
                iconY -= (mIconSize + mPadding);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);
    }

    void startDrag(BubbleView view) {
        removeView(view);
        mDragController.removeDropTarget(view);
        mDragController.startDrag(this, view);
    }

    @Override
    public void onDropCompleted(BubbleView target, DragObject d) {
        // TODO Auto-generated method stub

        if (target != null) {
            int tIndex = mApps.indexOf(target);
            int sIndex = mApps.indexOf(d.dragView);
            mApps.set(tIndex, d.dragView);
            mApps.set(sIndex, target);
        }
        mLauncher.getDragLayer().removeView(d.dragView);
        mDragController.addDropTarget(d.dragView);
        addView(d.dragView);
        update();
    }
}
