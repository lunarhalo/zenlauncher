
package com.cooeeui.brand.zenlauncher.unittest.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceUtil {
    final static String TAG = "Utils.DeviceUtil";

    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static String getAndroidId(Context context) {
        return android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
    }

    public static String getSimSerialNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimSerialNumber();
    }

    public static String getSerialNumber1() {
        return android.os.Build.SERIAL;
    }

    /**
     * getSerialNumber
     * 
     * @return result is same to getSerialNumber1()
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    public static void displayDevice(Context context) {
        String imei = getIMEI(context);
        String androidId = getAndroidId(context);
        String sn = getSerialNumber();
        String sim = getSimSerialNumber(context);
        Log.i(TAG, "isTestDevice:"
                + "\nIMEI:" + imei
                + "\nANDROID ID:" + androidId
                + "\nSerialNumber:" + sn
                + "\nSimSerialNumber:" + sim
                );
    }

    public static String getUniqueId(Context context) {
        String imei = getIMEI(context);
        String androidId = getAndroidId(context);
        String sn = getSerialNumber();
        String sim = getSimSerialNumber(context);
        String uniqueId = imei + androidId + sn + sim;
        return uniqueId;
    }
}
