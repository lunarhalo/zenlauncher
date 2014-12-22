
package com.cooeeui.brand.zenlauncher.applistlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.R;

public class AppNameViewGroup extends MyRelativeLayout implements IAppGroup {

    private AppListUtil util = null;
    private TextView nameTextView = null;
    private Button optionButton = null;
    private ClickButtonOnClickListener onClickListener = null;

    public AppListUtil getUtil() {
        return util;
    }

    public void setUtil(AppListUtil util) {
        this.util = util;
    }

    public ClickButtonOnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(ClickButtonOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public AppNameViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initViewData() {
        nameTextView = (TextView) this.findViewById(R.id.titleText);
        optionButton = (Button) this.findViewById(R.id.optionButton);
        nameTextView.setText(util.tabName[util.getTabNum()]);
        optionButton.setOnClickListener(onClickListener);
        optionButton.setTag(util.optionName);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
    }

    // @Override
    // protected void onLayout(boolean changed, int l, int t, int r, int b) {
    // // TODO Auto-generated method stub
    // for (int i = 0; i < this.getChildCount(); i++) {
    // View view = this.getChildAt(i);
    // int viewleft = view.getLeft();
    // int viewtop = view.getTop();
    // int viewright = view.getRight();
    // int viewbottom = view.getBottom();
    // view.layout(viewleft, viewtop, viewright, viewbottom);
    // }
    // }

    public void setTextName(String newName) {
        if (nameTextView != null) {
            nameTextView.setText(newName);
        }
    }

}
