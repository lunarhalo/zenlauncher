
package com.cooeeui.brand.zenlauncher.unittest.applistLayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;
import android.view.Window;

public class AppHostActivity extends Activity {
    private AppHostViewGroup appHostParent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        AppListUtil util = new AppListUtil(this);
        appHostParent = new AppHostViewGroup(this, util);
        LayoutParams lp = new LayoutParams(util.getAllScreenWidth(), util.getAllScreenHeight());
        this.setContentView(appHostParent, lp);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }
}
