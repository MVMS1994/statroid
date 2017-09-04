package subbiah.veera.statroid.data;

import android.util.Log;

import subbiah.veera.statroid.BuildConfig;

/**
 * Created by Veera.Subbiah on 24/08/17.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class Logger {
    private static final boolean isAppDebuggable = BuildConfig.DEBUG;
    private Logger() {}

    public static void d(String tag, Object msg) {
        if(isAppDebuggable) Log.d(tag, String.valueOf(msg));
    }

    public static void i(String tag, Object msg) {
        if(isAppDebuggable) Log.i(tag, String.valueOf(msg));
    }

    public static void e(String tag, Object msg) {
        Log.e(tag, String.valueOf(msg));
    }

    public static void e(String tag, Object msg, Throwable e) {
        Log.e(tag, String.valueOf(msg), e);
    }

    public static void v(String tag, Object msg) {
        if(isAppDebuggable) Log.v(tag, String.valueOf(msg));
    }
}
