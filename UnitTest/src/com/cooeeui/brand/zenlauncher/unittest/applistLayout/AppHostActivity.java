
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;

import com.cooeeui.brand.zenlauncher.unittest.R;

public class AppHostActivity extends Activity {
    private AppHostViewGroup appHostParent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.apphostlayout);
        AppListUtil util = new AppListUtil(this);
        appHostParent = (AppHostViewGroup) this.findViewById(R.id.appHostGroup);
        appHostParent.setUtil(util);
        appHostParent.initViewData();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }
}
