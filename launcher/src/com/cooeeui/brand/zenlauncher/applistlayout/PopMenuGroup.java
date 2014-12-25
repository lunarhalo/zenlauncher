
package com.cooeeui.brand.zenlauncher.applistlayout;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.R;

public class PopMenuGroup extends LinearLayout {
    private int mAllWidth = -1;
    private int mAllHeight = -1;
    private final int mChildCount = 4;
    private PopRelativeLayout mClassifyLayou = null;
    private PopRelativeLayout mUnloadLayou = null;
    private PopRelativeLayout mHideiconLayou = null;
    private PopRelativeLayout mZenSettingLayou = null;
    private PopRelativeLayout[] layoutChilds = new PopRelativeLayout[mChildCount];
    public final String mPopTag = "PopMenuItem";// 用于在点击事件的时候匹配是否为popmenu

    public PopMenuGroup(Context context, ClickButtonOnClickListener clickButtonOnClickListener) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundColor(Color.argb(0, 0, 0, 0));
        mClassifyLayou = new PopRelativeLayout(context, context.getResources().getString(
                R.string.classify), 0, clickButtonOnClickListener);
        this.addView(mClassifyLayou);
        layoutChilds[0] = mClassifyLayou;

        mUnloadLayou = new PopRelativeLayout(context, context.getResources().getString(
                R.string.unload), 1, clickButtonOnClickListener);
        this.addView(mUnloadLayou);
        layoutChilds[1] = mUnloadLayou;

        mHideiconLayou = new PopRelativeLayout(context, context.getResources().getString(
                R.string.hideicon), 2, clickButtonOnClickListener);
        this.addView(mHideiconLayou);
        layoutChilds[2] = mHideiconLayou;

        mZenSettingLayou = new PopRelativeLayout(context, context.getResources().getString(
                R.string.zen_settings), 3, clickButtonOnClickListener);
        this.addView(mZenSettingLayou);
        layoutChilds[3] = mZenSettingLayou;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int height = b - t;
        int width = r - l;
        if (mAllHeight != height || mAllWidth != width) {
            mAllHeight = height;
            mAllWidth = width;
            setChildSize();
        }
    }

    private void setChildSize() {

        int childHeight = mAllHeight / mChildCount;
        for (int i = 0; i < mChildCount; i++) {
            PopRelativeLayout layout = layoutChilds[i];
            LayoutParams lp = new LayoutParams(mAllWidth, childHeight);
            layout.setLayoutParams(lp);
            layout.setRelativeChildSize(mAllWidth, childHeight);
        }
    }

    private class PopRelativeLayout extends RelativeLayout {
        private TextView textView = null;
        private ImageView imageView_bottom = null;
        private ImageView imageView_left = null;
        private ImageView imageView_right = null;
        private ImageView imageView_top = null;
        private int imageColor = Color.argb(255, 255, 255, 255);

        public PopRelativeLayout(Context context, String layoutName, int position,
                ClickButtonOnClickListener clickButtonOnClickListener) {
            super(context);
            this.setOnClickListener(clickButtonOnClickListener);
            this.setTag(layoutName + mPopTag);
            textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setText(layoutName);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            this.addView(textView);

            imageView_bottom = new ImageView(context);
            imageView_bottom.setBackgroundColor(imageColor);
            this.addView(imageView_bottom);

            imageView_left = new ImageView(context);
            imageView_left.setBackgroundColor(imageColor);
            this.addView(imageView_left);

            imageView_right = new ImageView(context);
            imageView_right.setBackgroundColor(imageColor);
            this.addView(imageView_right);

            if (position == 0) {
                imageView_top = new ImageView(context);
                imageView_top.setBackgroundColor(imageColor);
                this.addView(imageView_top);
            }
        }

        public void setRelativeChildSize(int width, int height) {
            int imageHeight = 1;
            textView.setX(0);
            textView.setY(0);
            RelativeLayout.LayoutParams textLp = new
                    RelativeLayout.LayoutParams(width, height);
            textView.setLayoutParams(textLp);

            imageView_bottom.setX(0);
            imageView_bottom.setY(height - imageHeight);
            LayoutParams imagebottomlp = new LayoutParams(width, imageHeight);
            imageView_bottom.setLayoutParams(imagebottomlp);

            if (imageView_top != null) {
                imageView_top.setX(0);
                imageView_top.setY(0);
                imageView_top.setLayoutParams(imagebottomlp);
            }

            imageView_right.setX(width - imageHeight);
            imageView_right.setY(0);
            LayoutParams imagerightlp = new LayoutParams(imageHeight, height);
            imageView_right.setLayoutParams(imagerightlp);

            imageView_left.setX(0);
            imageView_right.setY(0);
            imageView_left.setLayoutParams(imagerightlp);
        }
    }
}
