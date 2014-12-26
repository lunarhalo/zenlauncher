
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.R;

public class TitleBar extends RelativeLayout {

    private TextView nameTextView = null;
    private Button optionButton = null;

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // find views.
        nameTextView = (TextView) this.findViewById(R.id.titleText);
        optionButton = (Button) this.findViewById(R.id.optionButton);
    }

    public void setUtil(AppListUtil util) {
        nameTextView.setText(util.tabName[util.getTabNum()]);
        optionButton.setTag(util.optionName);
    }

    public void setOnClickListener(ClickButtonOnClickListener onClickListener) {
        optionButton.setOnClickListener(onClickListener);
    }

    public void setTextName(String newName) {
        if (nameTextView != null) {
            nameTextView.setText(newName);
        }
    }

}
