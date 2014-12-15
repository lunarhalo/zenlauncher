
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.cooeeui.brand.zenlauncher.unittest.R;

public class AppTabViewGroup extends ViewGroup implements IAppGroup {
    private AppListUtil util = null;
    private ImageView tabImageView = null;
    private ClickButtonOnClickListener onClickListener = null;
    private int padding = -1;
    private int tabButtonWidth = -1;
    private int[] tabIconId = new int[] {
            R.drawable.applayout_favourite,
            R.drawable.applayout_communication,
            R.drawable.applayout_life,
            R.drawable.applayout_soclal,
            R.drawable.applayout_system,
            R.drawable.applayout_tool
    };

    public AppTabViewGroup(Context context, AppListUtil util,
            ClickButtonOnClickListener onClickListener) {
        super(context);
        this.util = util;
        this.onClickListener = onClickListener;
        // TODO Auto-generated constructor stub
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

    @Override
    public void initAddChildView() {
        // TODO Auto-generated method stub
        ImageView imageView = new ImageView(getContext());
        imageView.setLeft(0);
        imageView.setTop(0);
        imageView.setRight(util.getAllScreenWidth());
        imageView.setBottom((int) (util.getLineWidth() * util.getDensity()));
        imageView.setBackgroundResource(R.drawable.applayout_split_line);
        this.addView(imageView);
        addTabButtonView();
    }

    /**
     * 添加底边的tab
     */
    private void addTabButtonView() {
        // TODO Auto-generated method stub
        if (tabIconId.length == util.tabName.length) {
            int tabNum = tabIconId.length;
            tabButtonWidth = (int) ((util.getAllScreenWidth() / tabNum) * 0.8f);
            padding = (this.getWidth() - tabButtonWidth * tabNum) / (tabNum * 2);
            int viewtop = padding;
            int viewbottom = viewtop + tabButtonWidth + padding;
            tabImageView = new ImageView(getContext());
            changeTabLeft(util.getTabNum());
            tabImageView.setTop(viewbottom);
            tabImageView.setBottom(viewbottom + (int) (util.getLineWidth() * util.getDensity()));
            tabImageView.setBackgroundResource(R.drawable.applayout_split_line);
            this.addView(tabImageView);
            for (int i = 0; i < tabNum; i++) {
                Button tabButton = new Button(getContext());
                tabButton.setOnClickListener(onClickListener);
                tabButton.setTag(util.tabName[i]);
                tabButton.setBackgroundResource(tabIconId[i]);
                int left = padding * (2 * i + 1) + tabButtonWidth * i;
                tabButton.setLeft(left);
                tabButton.setRight(left + tabButtonWidth);
                tabButton.setTop(viewtop);
                tabButton.setBottom(viewbottom);
                this.addView(tabButton);
            }
        }
    }

    public void changeTabLeft(int num) {
        if (tabImageView != null) {
            int left = num * tabButtonWidth + padding * (2 * num + 1);
            tabImageView.setLeft(left);
            tabImageView.setRight(left + tabButtonWidth);
        }
    }
}
