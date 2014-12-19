
package com.cooeeui.brand.zenlauncher.scenes;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.scenes.ui.BubbleView;
import com.cooeeui.brand.zenlauncher.scenes.utils.BitmapUtils;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragSource;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;

public class Workspace extends FrameLayout implements DragSource, View.OnTouchListener {

    private Launcher mLauncher;
    private DragController mDragController;

    private static final int WORKSPACE_STATE_NORMAL = 0;
    private static final int WORKSPACE_STATE_DRAG = 1;
    private int mState = WORKSPACE_STATE_NORMAL;

    public static final int ICON_SIZE_MAX = 144;
    public static final int ICON_SIZE_MIN = 10;
    private int mIconSize;
    private int mPadding;
    private float[] mMidPoint = new float[2];
    private int mWidth;
    private int mHeight;

    private static final int BUBBLE_VIEW_CAPACITY = 9;
    private ArrayList<BubbleView> mBubbleViews = new ArrayList<BubbleView>(BUBBLE_VIEW_CAPACITY);

    public static final int EDIT_VIEW_ICON = 0x100;
    public static final int EDIT_VIEW_CHANGE = 0x101;
    public static final int EDIT_VIEW_DELETE = 0x102;

    private BubbleView mSelect;
    private View mSearchBar;
    private View mEditBottomView;

    private Bitmap mDefaultIcon;

    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Workspace(Context context) {
        super(context);
    }

    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
        mSearchBar = mLauncher.getDragLayer().findViewById(R.id.search_bar);

        // setup edit bottom view.
        mEditBottomView = mLauncher.getDragLayer().findViewById(R.id.edit_bottom_view);
        int[] ids = {
                R.id.edit_bottom_change_icon,
                R.id.edit_bottom_change_app,
                R.id.edit_bottom_delete
        };
        int[] tags = {
                EDIT_VIEW_ICON,
                EDIT_VIEW_CHANGE,
                EDIT_VIEW_DELETE
        };
        View v;
        for (int i = 0; i < ids.length; i++) {
            v = mLauncher.getDragLayer().findViewById(ids[i]);
            v.setOnClickListener(mLauncher);
            v.setTag(tags[i]);
        }

        BitmapUtils.setIconSize(ICON_SIZE_MAX);
    }

    public void startBind() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof BubbleView) {
                mDragController.removeDropTarget((DropTarget) v);
                BubbleView view = (BubbleView) v;
                view.clearBitmap();
                removeView(view);
            }
        }
        mBubbleViews.clear();
    }

    public void finishBind() {
        update();
    }

    private void addBubbleView(ShortcutInfo info, Bitmap b) {
        BubbleView v = new BubbleView(mLauncher, b);
        v.setTag(info);
        addView(v);
        mBubbleViews.add(v);
        v.setOnClickListener(mLauncher);
        v.setOnLongClickListener(mLauncher);

        v.setOnTouchListener(this);

        mDragController.addDropTarget(v);
    }

    public void addBubbleViewFromBind(ShortcutInfo info) {
        Bitmap b = info.mIcon;
        if (b == null) {
            b = getDefaultIcon();
            info.mRecycle = false;
        }

        addBubbleView(info, b);
    }

    public void addBubbleView(ShortcutInfo info) {
        Bitmap b = info.mIcon;
        int position = mBubbleViews.size();

        addBubbleView(info, b);
        mSelect = mBubbleViews.get(position);

        LauncherModel.addItemToDatabase(mLauncher, info, position);
    }

    private Bitmap getDefaultIcon() {
        if (mDefaultIcon == null) {
            mDefaultIcon = BitmapUtils.getIcon(Resources.getSystem(),
                    android.R.mipmap.sym_def_app_icon);
        }
        return mDefaultIcon;
    }

    public void changeIcon(int iconId) {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();

        if (i.mIconId == iconId) { // same icon
            return;
        }
        if (i.mRecycle) {
            mSelect.clearBitmap();
        }

        Bitmap b = BitmapUtils.getIcon(mLauncher.getResources(), iconId);
        i.mRecycle = true;
        i.mIconId = iconId;
        if (b == null) {
            b = getDefaultIcon();
            i.mRecycle = false;
            i.mIconId = -1;
        }

        mSelect.changeBitmap(b);

        LauncherModel.updateItemInDatabase(mLauncher, i);
    }

    public void changeBubbleView(ShortcutInfo info) {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();
        if (i.mRecycle) {
            mSelect.clearBitmap();
        }
        i.intent = info.intent;
        i.mIcon = info.mIcon;
        i.mIconId = -1;

        Bitmap b = info.mIcon;
        mSelect.changeBitmap(b);

        LauncherModel.updateItemInDatabase(mLauncher, i);
    }

    public void removeBubbleView() {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();
        int index = mBubbleViews.indexOf(mSelect);

        mBubbleViews.remove(mSelect);
        removeView(mSelect);
        mDragController.removeDropTarget(mSelect);
        update();
        LauncherModel.deleteItemFromDatabase(mLauncher, i);

        if (i.mRecycle) {
            mSelect.clearBitmap();
        }

        if (mBubbleViews.size() <= 0) {
            stopDrag();
            return;
        }
        if (index == mBubbleViews.size()) {
            mSelect = mBubbleViews.get(index - 1);
        } else {
            mSelect = mBubbleViews.get(index);
        }
    }

    private void moveBubbleView(int position, float x, float y) {
        BubbleView v = mBubbleViews.get(position);
        if (v != null) {
            v.move(x, y);
        }
    }

    void initSize() {
        int length;
        if (mWidth > mHeight) {
            mMidPoint[0] = (mWidth - mHeight) / 2;
            mMidPoint[1] = 0;
            length = mHeight;

        } else {
            mMidPoint[0] = 0;
            mMidPoint[1] = (mHeight - mWidth) / 2;
            length = mWidth;
        }
        mPadding = length / 10;
        mIconSize = (length - mPadding * 2) / 3;
        mMidPoint[1] += (mIconSize + mPadding);
        if (mIconSize > ICON_SIZE_MAX) {
            int offset = mIconSize - ICON_SIZE_MAX;
            mPadding += offset;
            mMidPoint[0] += offset / 2;
            mMidPoint[1] += offset / 2;
            mIconSize = ICON_SIZE_MAX;
        } else if (mIconSize < ICON_SIZE_MIN) {
            mIconSize = ICON_SIZE_MIN;
        }

        BitmapUtils.setIconSize(mIconSize);
    }

    public void update() {
        int count = mBubbleViews.size();

        Rect r = new Rect();
        getGlobalVisibleRect(r);
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
            moveBubbleView(count - 1, iconX, iconY);
        } else if (rNum == 2) {
            iconX = startX + (mIconSize + mPadding) / 2;
            iconY = startY - (mIconSize + mPadding) * (count / 3);
            moveBubbleView(count - 2, iconX, iconY);
            moveBubbleView(count - 1, iconX + mIconSize + mPadding, iconY);
        }

        rNum = count - rNum;
        iconX = startX;
        iconY = startY;
        for (int i = 0; i < rNum; i++) {
            moveBubbleView(i, iconX, iconY);
            iconX += (mIconSize + mPadding);
            if ((i + 1) % 3 == 0) {
                iconX = startX;
                iconY -= (mIconSize + mPadding);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mWidth != widthSize || mHeight != heightSize) {
            mWidth = widthSize;
            mHeight = heightSize;
            initSize();
        }

        int sizeMeasureSpec = MeasureSpec.makeMeasureSpec(mIconSize, MeasureSpec.EXACTLY);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.measure(sizeMeasureSpec, sizeMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (mState == WORKSPACE_STATE_NORMAL) {
            return false;
        }

        if (action == MotionEvent.ACTION_DOWN && v instanceof BubbleView) {
            BubbleView bv = (BubbleView) v;
            startDrag(bv);
            return true;
        }
        return false;
    }

    private void showEditViews() {
        mSearchBar.setVisibility(View.INVISIBLE);
        mEditBottomView.setVisibility(View.VISIBLE);
    }

    private void hideEditViews() {
        mEditBottomView.setVisibility(View.INVISIBLE);
        mSearchBar.setVisibility(View.VISIBLE);
    }

    public void startDrag(BubbleView view) {
        if (mState != WORKSPACE_STATE_DRAG) {
            mState = WORKSPACE_STATE_DRAG;
            showEditViews();
        }
        mSelect = view;
        removeView(view);
        mDragController.removeDropTarget(view);
        mDragController.startDrag(this, view);
    }

    public void stopDrag() {
        if (mState != WORKSPACE_STATE_NORMAL) {
            mState = WORKSPACE_STATE_NORMAL;
            hideEditViews();
        }
    }

    public boolean isFull() {
        return mBubbleViews.size() >= BUBBLE_VIEW_CAPACITY;
    }

    @Override
    public void onDropCompleted(BubbleView target) {
        if (target != null) {
            ShortcutInfo si = (ShortcutInfo) mSelect.getTag();
            ShortcutInfo ti = (ShortcutInfo) target.getTag();
            int tIndex = mBubbleViews.indexOf(target);
            int sIndex = mBubbleViews.indexOf(mSelect);

            LauncherModel.modifyItemInDatabase(mLauncher, si, tIndex);
            mBubbleViews.set(tIndex, mSelect);
            mBubbleViews.set(sIndex, target);
            LauncherModel.modifyItemInDatabase(mLauncher, ti, sIndex);
        }
        mLauncher.getDragLayer().removeView(mSelect);
        mDragController.addDropTarget(mSelect);
        addView(mSelect);
        update();
    }
}
