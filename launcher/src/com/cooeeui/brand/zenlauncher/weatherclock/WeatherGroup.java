
package com.cooeeui.brand.zenlauncher.weatherclock;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.R;

public class WeatherGroup extends RelativeLayout {

    private ImageView weatherIamge = null;
    private TextView cityTextView = null;
    private TextView tempTextView = null;

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
        setWeatherImage(R.drawable.weather_sun);
        setCityName("Newyork");
        setTemp(-10, 20);
    }

    private void setTemp(int minTemp, int maxTemp) {
        // TODO Auto-generated method stub
        String temp = minTemp + " / " + maxTemp;
        tempTextView.setText(temp);
    }

    private void setCityName(String cityName) {
        // TODO Auto-generated method stub
        cityTextView.setText(cityName);
    }

    private void setWeatherImage(int image) {
        // TODO Auto-generated method stub
        weatherIamge.setBackgroundResource(image);
    }

    public void setChildViewSize(int allWidth, int allHeight) {
        int weatherX = (int) (allWidth * 0.3f);
        int weatherWidth = allHeight;
        int weatherheight = weatherWidth;
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
}
