
package com.cooeeui.brand.zenlauncher.weatherdata;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.R;

public class WeatherGroupUtil {
    private ImageView weatherImage = null;
    private TextView cityName = null;
    private TextView tempName = null;
    private ClickListener clickListener = null;
    private final String weatherImageTag = "weatherImage";
    private final String cityNameTag = "cityName";
    private final String tempNameTag = "tempName";
    private Context context = null;
    private WeatherBroadcast weatherBroadcast = null;
    private SharedPreferences sharepreference = null;
    private IntentFilter mFilter = null;
    private Launcher mLauncher = null;
    private View viewParent = null;

    public WeatherGroupUtil(Context context, ImageView weatherImage,
            TextView cityName,
            TextView tempName) {
        this.context = context;
        this.weatherImage = weatherImage;
        this.cityName = cityName;
        this.tempName = tempName;
        sharepreference = PreferenceManager
                .getDefaultSharedPreferences(context);
        clickListener = new ClickListener();
        weatherImage.setOnClickListener(clickListener);
        weatherImage.setTag(weatherImageTag);
        cityName.setOnClickListener(clickListener);
        cityName.setTag(cityNameTag);
        tempName.setOnClickListener(clickListener);
        tempName.setTag(tempNameTag);
        weatherBroadcast = new WeatherBroadcast();
        mFilter = new IntentFilter();
        mFilter.addAction(Parameter.BROADCASE_WeatherInLandSearch);
        mFilter.addAction(Parameter.BROADCASE_WeatherCurveFlush);
        mFilter.addAction(Parameter.UPDATE_RESULT);

        mFilter.addAction(Parameter.BROADCASE_WeatherForeignSearch);
        mFilter.addAction(Parameter.BROADCASE_WeatherCurveForeignFlush);
        mFilter.addAction(Parameter.UPDATE_RESULT_FOREIGN);
        mFilter.addAction(Parameter.BROADCASE_WeatherCFFlush);

        mFilter.addAction(Intent.ACTION_TIME_TICK);

        new UpDateWeatherHelper(context);
        updateWeatherShow(context.getResources().getString(R.string.default_cityname), "晴", "10",
                "-5");
        getCurrentWeather();
    }

    private void getCurrentWeather() {
        if (Parameter.enable_google_version) {
            if (sharepreference.getBoolean("numberweatherstate", false)) {
                Weather weather = NumberClockHelper
                        .getWeatherForeign(sharepreference);
                if (weather != null && weather.getList() != null
                        && weather.getList().size() == 5) {
                    updateWeatherShow(weather.getWeathercity(),
                            weather.getWeathercode(), weather.getList().get(0)
                                    .getHightmp(), weather.getList().get(0)
                                    .getLowtmp());
                }
            }
        } else {
            if (sharepreference.getBoolean("inlandnumberweatherstate", false)) {
                WeatherEntity weatherentity = NumberClockHelper
                        .getWeatherInland(sharepreference);
                if (weatherentity != null && weatherentity.getDetails() != null
                        && weatherentity.getDetails().size() == 5) {
                    updateWeatherShow(weatherentity.getCity(),
                            weatherentity.getCondition(), weatherentity
                                    .getDetails().get(0).getHight(),
                            weatherentity.getDetails().get(0).getLow());
                }
            }
        }
    }

    private class ClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag instanceof String) {
                String keyTag = (String) tag;
                int currentState = -1;
                if (keyTag.equals(cityNameTag) || keyTag.equals(tempNameTag)) {
                    currentState = 1;
                } else if (keyTag.equals(weatherImageTag)) {
                    currentState = 0;
                }
                if (viewParent == null) {
                    viewParent = mLauncher.getDragLayer();
                }
                ContentActivity.setDragView(viewParent);
                Intent cityintent = new Intent(context, ContentActivity.class);
                cityintent.putExtra("currentState", currentState);
                cityintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(cityintent);
            }
        }
    }

    private void updateWeatherShow(String cityName, String weathercondition,
            String HighTmp, String lowTmp) {

        if (Parameter.enable_google_version) {
            weatherImage.setBackgroundResource(NumberClockHelper.codeForPath(weathercondition));
        } else {
            weatherImage.setBackgroundResource(NumberClockHelper
                    .getResourceFromString(weathercondition));
        }
        this.cityName.setText(cityName);
        String temp = lowTmp + " / " + HighTmp;
        this.tempName.setText(temp);
    }

    private class WeatherBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Parameter.BROADCASE_WeatherInLandSearch)
                    || action.equals(Parameter.BROADCASE_WeatherCurveFlush)
                    || action.equals(Parameter.UPDATE_RESULT)) {
                Bundle bundle = intent.getExtras();
                WeatherEntity inlandweather = (WeatherEntity) bundle
                        .getSerializable(Parameter.SerializableBroadcastName);
                if (inlandweather != null && inlandweather.getDetails() != null
                        && inlandweather.getDetails().size() == 5) {
                    updateWeatherShow(inlandweather.getCity(),
                            inlandweather.getCondition(), inlandweather
                                    .getDetails().get(0).getHight(), inlandweather
                                    .getDetails().get(0).getLow());
                    NumberClockHelper.setWeatherInland(sharepreference,
                            inlandweather);
                }
            } else if (action.equals(Parameter.BROADCASE_WeatherForeignSearch)
                    || action.equals(Parameter.BROADCASE_WeatherCurveForeignFlush)
                    || action.equals(Parameter.UPDATE_RESULT_FOREIGN)
                    || action.equals(Parameter.BROADCASE_WeatherCFFlush)) {
                Bundle bundle = intent.getExtras();
                Weather weather = (Weather) bundle
                        .getSerializable(Parameter.SerializableBroadcastName);
                if (weather != null && weather.getList() != null
                        && weather.getList().size() == 5) {
                    updateWeatherShow(weather.getWeathercity(),
                            weather.getWeathercode(), weather.getList().get(0)
                                    .getHightmp(), weather.getList().get(0)
                                    .getLowtmp());
                    NumberClockHelper.setWeatherForeign(sharepreference, weather);
                }
            } else if (action.equals(Intent.ACTION_TIME_TICK)) {
                Calendar c = Calendar.getInstance();// 可以对每个时间域单独修改
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                if (Parameter.enable_google_version) {
                    if (sharepreference.getBoolean("numberweatherstate", false)) {
                        Weather weather = NumberClockHelper
                                .getWeatherForeign(sharepreference);
                        if (weather != null && weather.getList() != null
                                && weather.getList().size() == 5) {
                            if (hour == 18 && minute == 0) {
                                updateWeatherShow(weather.getWeathercity(),
                                        weather.getWeathercode(), weather.getList()
                                                .get(0).getHightmp(), weather
                                                .getList().get(0).getLowtmp());
                            } else if (hour == 0 && minute == 0) {
                                updateWeatherShow(weather.getWeathercity(),
                                        weather.getWeathercode(), weather.getList()
                                                .get(1).getHightmp(), weather
                                                .getList().get(1).getLowtmp());
                            }
                        }
                    }
                } else {
                    if (sharepreference.getBoolean("inlandnumberweatherstate",
                            false)) {
                        WeatherEntity weatherentity = NumberClockHelper
                                .getWeatherInland(sharepreference);
                        if (weatherentity != null
                                && weatherentity.getDetails() != null
                                && weatherentity.getDetails().size() == 5) {
                            if (hour == 18 && minute == 0) {
                                updateWeatherShow(weatherentity.getCity(),
                                        weatherentity.getCondition(), weatherentity
                                                .getDetails().get(0).getHight(),
                                        weatherentity.getDetails().get(0).getLow());
                            } else if (hour == 0 && minute == 0) {
                                updateWeatherShow(weatherentity.getCity(),
                                        weatherentity.getCondition(), weatherentity
                                                .getDetails().get(1).getHight(),
                                        weatherentity.getDetails().get(1).getLow());
                            }
                        }
                    }
                }
            }
        }
    }

    public void register() {
        context.registerReceiver(weatherBroadcast, mFilter);
    }

    public void unRegister() {
        context.unregisterReceiver(weatherBroadcast);
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }
}
