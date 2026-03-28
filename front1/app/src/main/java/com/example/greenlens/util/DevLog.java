package com.example.greenlens.util;

import android.util.Log;

import com.example.greenlens.constant.AppConf;

public class DevLog {
    private static final String DEFAULT_TAG = "DevLog";

    public static void d(String message) {
        if (AppConf.IS_DEBUG) {
            Log.d(DEFAULT_TAG, message);
        }
    }

    public static void d(String tag, String message) {
        if (AppConf.IS_DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void i(String message) {
        if (AppConf.IS_DEBUG) {
            Log.i(DEFAULT_TAG, message);
        }
    }

    public static void i(String tag, String message) {
        if (AppConf.IS_DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void w(String message) {
        if (AppConf.IS_DEBUG) {
            Log.w(DEFAULT_TAG, message);
        }
    }

    public static void w(String tag, String message) {
        if (AppConf.IS_DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void e(String message) {
        if (AppConf.IS_DEBUG) {
            Log.e(DEFAULT_TAG, message);
        }
    }

    public static void e(String tag, String message) {
        if (AppConf.IS_DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (AppConf.IS_DEBUG) {
            Log.e(tag, message, throwable);
        }
    }

    public static void v(String message) {
        if (AppConf.IS_DEBUG) {
            Log.v(DEFAULT_TAG, message);
        }
    }

    public static void v(String tag, String message) {
        if (AppConf.IS_DEBUG) {
            Log.v(tag, message);
        }
    }
}