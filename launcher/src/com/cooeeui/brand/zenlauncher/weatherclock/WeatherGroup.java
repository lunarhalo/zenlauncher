
package com.cooeeui.brand.zenlauncher.weatherclock;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.weatherdata.WeatherGroupUtil;

public class WeatherGroup extends RelativeLayout {

    private ImageView weatherIamge = null;
    private TextView cityTextView = null;
    private TextView tempTextView = null;
    private WeatherGroupUtil mWeatherGroupUtil = null;
    private Launcher mLauncher = null;

    public WeatherGroup(Context context) {
        super(context);
        weatherIamge = new ImageView(context);
        cityTextView = new TextView(context);
        cityTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        cityTextView.setTextColor(Color.WHITE);
        tempTextView = new TextView(context);
        tempTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tempTextView.setTextColor(Color.WHITE);
        this.addView(cityTextView);
        this.addView(tempTextView);
        this.addView(weatherIamge);

        mWeatherGroupUtil = new WeatherGroupUtil(context, weatherIamge, cityTextView, tempTextView);
    }

    public void setChildViewSize(int allWidth, int allHeight) {
        int weatherX = (int) (allWidth * 0.3f);
        int weatherheight = allHeight;
        int weatherWidth = (int) (weatherheight * 1.38f);// 1.38，为天气图标的长宽比
        int cityX = (int) ((weatherX + weatherWidth) * 1.04f);
        int cityWidth = allWidth - cityX;
        int cityHeight = (int) (allHeight / 2f);
        int tempX = cityX;
        int tempY = cityHeight;
        weatherIamge.setX(weatherX);
        weatherIamge.setY(0);
        LayoutParams weatherlp = new LayoutParams(weatherWidth, weatherheight);
        weatherIamge.setLayoutParams(weatherlp);
        cityTextView.setX(cityX);
        cityTextView.setY(0);
        LayoutParams citylp = new LayoutParams(cityWidth, cityHeight);
        cityTextView.setLayoutParams(citylp);
        tempTextView.setX(tempX);
        tempTextView.setY(tempY);
        tempTextView.setLayoutParams(citylp);
    }

    public void register() {
        mWeatherGroupUtil.register();
    }

    public void unRegister() {
        mWeatherGroupUtil.unRegister();
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
        mWeatherGroupUtil.setLauncher(mLauncher);
    }
}
