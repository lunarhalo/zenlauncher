
package com.cooeeui.brand.zenlauncher;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.cooeeui.brand.zenlauncher.applistlayout.AppHostActivity;

public class Workspace extends LinearLayout {

    private float touchDownX = -1;
    private float touchDownY = -1;
    private float detalY = 100;
    private boolean isApplistShow = false;
    private Intent appHostIntent = null;

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        boolean ret = super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = x;
                touchDownY = y;
                Log.v("", "whj tesr ACTION_DOWN touchDownYy is " + touchDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                if ((touchDownY - y) >= detalY) {
                    showApplistView();
                }
                break;
            case MotionEvent.ACTION_UP:
                touchDownX = -1;
                touchDownY = -1;
                isApplistShow = false;
                break;
            default:
                break;
        }
        return ret;
    }

    private void showApplistView() {
        // TODO Auto-generated method stub
        if (!isApplistShow) {
            isApplistShow = true;
            if (appHostIntent == null) {
                appHostIntent = new Intent(getContext(), AppHostActivity.class);
            }
            getContext().startActivity(appHostIntent);
            Log.v("", "whj tesr isApplistShow is " + isApplistShow);
        }
    }
}
