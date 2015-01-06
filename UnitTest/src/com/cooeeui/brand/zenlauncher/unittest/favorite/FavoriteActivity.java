
package com.cooeeui.brand.zenlauncher.unittest.favorite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.cooeeui.brand.zenlauncher.unittest.R;
import com.cooeeui.brand.zenlauncher.unittest.favorite.MonitorService.AppData;

public class FavoriteActivity extends Activity {

    private MonitorService mService;

    private ArrayList<AppData> applist = new ArrayList<AppData>();

    private LinearLayout layout;

    private DataView view;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("launcher123", "onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MonitorService.LocalBinder binder = (MonitorService.LocalBinder) service;
            mService = binder.getService();

            updateData();
            view.invalidate();
            Log.e("launcher123", "onServiceConnected");
        }
    };

    public void updateData() {
        if (mService == null) {
            return;
        }

        applist.clear();
        HashMap<ComponentName, AppData> datas = mService.getLastApp();

        for (AppData data : datas.values()) {
            applist.add(data);
        }

        Collections.sort(applist, new Comparator<AppData>() {
            @Override
            public int compare(AppData left, AppData right) {
                return (int) (right.count - left.count);
            }
        });

        int num = applist.size();
        for (int i = 0; i < num; i++) {
            Log.e("launcher123", "name = " + applist.get(i).name.getClassName() + "    count = "
                    + applist.get(i).count);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite);
        layout = (LinearLayout) findViewById(R.id.favorite_layout);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view = new DataView(this);
        layout.addView(view, params);

        Intent intent = new Intent(this, MonitorService.class);

        startService(intent);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unbindService(conn);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();
        view.invalidate();
        Log.e("launcher123", "onResume");
    }

    public class DataView extends View {

        private Paint mPaint;

        public DataView(Context context) {
            super(context);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.BLUE);
            mPaint.setTextSize(30);
        }

        public DataView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public DataView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.WHITE);

            int num = applist.size();
            if (num > 20) {
                num = 20;
            }
            for (int i = 0; i < num; i++) {
                String text = "name = " + applist.get(i).name.getClassName() + "    count = "
                        + applist.get(i).count;
                canvas.drawText(text, 20, 100 + i * 50, mPaint);
            }
        }
    }
}
