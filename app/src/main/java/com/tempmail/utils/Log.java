package com.tempmail.utils;


import com.tempmail.BuildConfig;

/**
 * This class includes Log methods to be used in application.
 */
public class Log {
    private static boolean isActive = BuildConfig.DEBUG;

    public static void i(String tag, String msg) {
        if (isActive)
            android.util.Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isActive)
            android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isActive)
            android.util.Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isActive)
            android.util.Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isActive)
            android.util.Log.w(tag, msg);
    }
}