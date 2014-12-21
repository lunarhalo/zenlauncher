
package com.cooeeui.brand.zenlauncher.weatherclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

public class WeatherClockGroup extends RelativeLayout {
    private ClockGroup clockGroup = null;
    private DateGroup dateGroup = null;
    private WeatherGroup weatherGroup = null;
    private int oldWidth = -100;
    private int oldHeight = -100;
    private Time time = null;
    private static final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;
    private static final String ACTION_TIME_CHANGED = Intent.ACTION_TIME_CHANGED;
    private static final String ACTION_TIME_TICK = Intent.ACTION_TIME_TICK;
    private MyTimeBroadCast myTimeBroadCast = null;
    private boolean isConnecNet = false;// 判断手机是否连上网络
    private ConnectivityManager connectivityManager = null;
    private ClickIntent clickIntent = null;

    public WeatherClockGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        // this.setBackgroundColor(Color.WHITE);
        clickIntent = new ClickIntent(context);
        clockGroup = new ClockGroup(context);
        clockGroup.setTag("clock");
        clockGroup.setOnClickListener(clickIntent);
        dateGroup = new DateGroup(context);
        dateGroup.setTag("calendar");
        dateGroup.setOnClickListener(clickIntent);
        weatherGroup = new WeatherGroup(context);
        weatherGroup.setTag("weather");
        weatherGroup.setOnClickListener(clickIntent);
        this.addView(clockGroup);
        this.addView(dateGroup);
        this.addView(weatherGroup);
        isConnecNet = getConnecNet();
        time = new Time();
        myTimeBroadCast = new MyTimeBroadCast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DATE_CHANGED);
        intentFilter.addAction(ACTION_TIME_CHANGED);
        intentFilter.addAction(ACTION_TIME_TICK);
        context.registerReceiver(myTimeBroadCast, intentFilter);
        // TODO Auto-generated constructor stub
    }

    /**
     * 获得此时手机是否连上网络
     * 
     * @return
     */
    private boolean getConnecNet() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) getContext().getSystemService(
                    Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    private void setChildViewSize() {
        // TODO Auto-generated method stub
        int clockWidth = (int) (oldWidth * 0.7f);
        int clockHeight = (int) (oldHeight * 0.67f);
        int dateWidth = oldWidth - clockWidth;
        int dataHeight = clockHeight;
        int weatherHeight = oldHeight - clockHeight;
        int weatherWidth = oldWidth;
        clockGroup.setX(0);
        clockGroup.setY(0);
        LayoutParams clockLp = new LayoutParams(clockWidth, clockHeight);
        clockGroup.setLayoutParams(clockLp);
        clockGroup.setChildViewSize(clockWidth, clockHeight);
        dateGroup.setX(clockWidth);
        dateGroup.setY(0);
        LayoutParams dateLp = new LayoutParams(dateWidth, dataHeight);
        dateGroup.setLayoutParams(dateLp);
        dateGroup.setChildViewSize(dateWidth, dataHeight);
        weatherGroup.setX(0);
        weatherGroup.setY(clockHeight);
        LayoutParams weatherLp = new LayoutParams(weatherWidth, weatherHeight);
        weatherGroup.setLayoutParams(weatherLp);
        weatherGroup.setChildViewSize(weatherWidth, weatherHeight);
        changeTimeAndDate();
    }

    private void changeTimeAndDate() {
        // TODO Auto-generated method stub
        time.setToNow();
        int hour = time.hour;
        int minute = time.minute;
        int week = time.weekDay;
        int mouth = time.month;
        int day = time.monthDay;
        Log.v("", "changeTimeAndDate hour is " + hour + " minute is " + minute + " week is " + week
                + " mouth is " + mouth + " day is " + day);
        clockGroup.setImageViewByTime(hour, minute);
        dateGroup.setWeekTextView(week);
        dateGroup.setMouthTextView(mouth);
        dateGroup.setDateImage(day);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        int width = r - l;
        int height = b - t;
        if (oldWidth != width || oldHeight != height) {
            oldHeight = height;
            oldWidth = width;
            setChildViewSize();
        }
    }

    private class MyTimeBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals(ACTION_TIME_TICK) || action.equals(ACTION_DATE_CHANGED)
                    || action.equals(ACTION_TIME_CHANGED)) {
                changeTimeAndDate();
            }
        }

    }
}
