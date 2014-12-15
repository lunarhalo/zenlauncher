
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.unittest.R;

public class AppNameViewGroup extends ViewGroup implements IAppGroup {

    private AppListUtil util = null;
    private TextView nameTextView = null;
    private ClickButtonOnClickListener onClickListener = null;

    public AppNameViewGroup(Context context, AppListUtil util,
            ClickButtonOnClickListener onClickListener) {
        super(context);
        this.util = util;
        this.onClickListener = onClickListener;
        // TODO Auto-generated constructor stub

    }

    @Override
    public void initAddChildView() {
        ImageView lineImageView = new ImageView(getContext());
        lineImageView.setBackgroundResource(R.drawable.applayout_split_line);
        lineImageView.setLeft(0);
        lineImageView.setTop(this.getHeight() - (int) (util.getLineWidth() * util.getDensity()));
        lineImageView.setRight(util.getAllScreenHeight());
        lineImageView.setBottom(this.getHeight());
        this.addView(lineImageView);
        nameTextView = new TextView(getContext());
        int textViewHeight = this.getHeight();
        int textViewX = (int) (this.getWidth() * 0.02f);
        int textViewY = (this.getHeight() - textViewHeight) / 2;
        nameTextView.setLeft(textViewX);
        nameTextView.setTop(textViewY);
        nameTextView.setRight(this.getWidth());
        nameTextView.setBottom(textViewHeight + textViewY);
        float textSize = 25;
        nameTextView.setTextSize(textSize);
        nameTextView.setText(util.tabName[util.getTabNum()]);
        this.addView(nameTextView);
        Button optionButton = new Button(getContext());
        optionButton.setOnClickListener(onClickListener);
        optionButton.setTag(util.optionName);
        int optionWidth = (int) (this.getWidth() * 0.1f);
        int optionX = this.getWidth() - optionWidth - (int) (this.getWidth() * 0.01f);
        int optionY = (int) ((this.getHeight() - optionWidth) / 2f);
        optionButton.setLeft(optionX);
        optionButton.setTop(optionY);
        optionButton.setRight(optionWidth + optionX);
        optionButton.setBottom(optionWidth + optionY);
        optionButton.setBackgroundResource(R.drawable.applayout_option);
        this.addView(optionButton);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        for (int i = 0; i < this.getChildCount(); i++) {
            View view = this.getChildAt(i);
            int viewleft = view.getLeft();
            int viewtop = view.getTop();
            int viewright = view.getRight();
            int viewbottom = view.getBottom();
            view.layout(viewleft, viewtop, viewright, viewbottom);
        }
    }

    public void setTextName(String newName) {
        if (nameTextView != null) {
            nameTextView.setText(newName);
        }
    }

}
