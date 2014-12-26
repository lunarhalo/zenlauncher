
package com.cooeeui.brand.zenlauncher.weatherdata;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.weatherdata.NumberWeatherwebservice.FLAG_UPDATE;

public class UpDateWeatherHelper {
    private static Context context;
    public static final int MSG_AUTOUPDATE_INLANDSUCCESS = 1;
    public static final int MSG_AUTOUPDATE_INLANDFAILURE = 2;
    public static final int MSG_AUTOUPDATE_NETWORK_FAILURE = 3;
    public static final int MSG_AUTOUPDATE_FOREIGNSUCCESS = 4;
    public static final int MSG_FOREIGNNETWORK_FAILURE = 5;
    public static final int MSG_FOREIGNFAILURE = 6;
    public static Handler mHandler = null;

    public UpDateWeatherHelper(Context context) {
        UpDateWeatherHelper.context = context;
        mHandler = new Handler() {
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                    case MSG_FOREIGNFAILURE:
                        Toast.makeText(
                                UpDateWeatherHelper.context,
                                UpDateWeatherHelper.context.getResources().getString(
                                        R.string.Flushfailed_foreign),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_AUTOUPDATE_INLANDSUCCESS:
                        Bundle bundle = (Bundle) msg.obj;
                        WeatherEntity weather = (WeatherEntity) bundle
                                .getSerializable(Parameter.SerializableAutoUpdateName);
                        if (weather != null && weather.getDetails() != null
                                && weather.getDetails().size() == 5) {
                            Intent intent = new Intent();
                            intent.setAction(Parameter.UPDATE_RESULT);
                            Bundle sendbundle = new Bundle();
                            sendbundle.putSerializable(
                                    Parameter.SerializableBroadcastName, weather);
                            intent.putExtras(sendbundle);
                            UpDateWeatherHelper.context.sendBroadcast(intent);
                            NumberWeatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_SUCCES;
                            Log.d("mytag", "更新成功");
                            Toast.makeText(UpDateWeatherHelper.context, "更新成功", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            NumberWeatherwebservice.Update_Result_Flag = FLAG_UPDATE.WEBSERVICE_ERROR;
                        }
                        break;
                    case MSG_AUTOUPDATE_INLANDFAILURE:
                        Toast.makeText(
                                UpDateWeatherHelper.context,
                                UpDateWeatherHelper.context.getResources().getString(
                                        R.string.flushfailed), Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case MSG_AUTOUPDATE_NETWORK_FAILURE:
                        Toast.makeText(
                                UpDateWeatherHelper.context,
                                UpDateWeatherHelper.context.getResources().getString(
                                        R.string.networkerror), Toast.LENGTH_SHORT)
                                .show();
                        break;

                    case MSG_AUTOUPDATE_FOREIGNSUCCESS:
                        Bundle bundleauto = (Bundle) msg.obj;
                        Weather weatherauto = (Weather) bundleauto
                                .getSerializable(Parameter.SerializableAutoUpdateForeignName);
                        if (weatherauto != null && weatherauto.getList() != null
                                && weatherauto.getList().size() == 5) {
                            Intent intent = new Intent();
                            intent.setAction(Parameter.UPDATE_RESULT_FOREIGN);
                            Bundle sendbundle = new Bundle();
                            sendbundle.putSerializable(
                                    Parameter.SerializableBroadcastName,
                                    weatherauto);
                            intent.putExtras(sendbundle);
                            UpDateWeatherHelper.context.sendBroadcast(intent);
                            NumberWeatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_SUCCES;
                            Log.d("mytag", "更新成功");
                            Toast.makeText(UpDateWeatherHelper.context, "更新成功", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            NumberWeatherwebservice.Update_Result_Flag = FLAG_UPDATE.WEBSERVICE_ERROR;
                        }
                        break;
                    case MSG_FOREIGNNETWORK_FAILURE:
                        Toast.makeText(
                                UpDateWeatherHelper.context,
                                UpDateWeatherHelper.context.getResources().getString(
                                        R.string.networkerror_foreign),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }
}
