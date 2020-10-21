package github.ryuunoakaihitomi.poweract.internal;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Debug;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogPrinter;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import github.ryuunoakaihitomi.poweract.BuildConfig;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.ReflectionUtils;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;

public final class Initializer {

    private static final String TAG = "Initializer";

    static {
        // For copyright protection.
        new LogPrinter(Log.WARN, "PowerAct").println("http://www.apache.org/licenses/LICENSE-2.0.html");
        // To load {@link ReflectionUtils#removeRestriction()} asap.
        ReflectionUtils.findClass(null);
        /* Debug special models' environment. Must be disabled in release version to avoid flooding log without ExternalUtils#enableLog()'s control. */
        if (BuildConfig.DEBUG) {
            printAllPublicConstants("build:", Build.class);
            printAllPublicConstants("version:", Build.VERSION.class);
            printMap("env", System.getenv());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DebugLog.i(TAG, "uname:" + Os.uname());
                SparseArray<String> sc = Utils.getClassIntApiConstant(OsConstants.class, "_SC");
                for (int i = 0; i < sc.size(); i++) {
                    try {
                        DebugLog.i(TAG, "sysconf:" + Arrays.asList(sc.valueAt(i), Os.sysconf(sc.keyAt(i))));
                    } /* ErrnoException */ catch (Exception e) {
                        DebugLog.e(TAG, "static initializer: " + e.getMessage() + " " + sc.valueAt(i));
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    printMap("art_stat", Debug.getRuntimeStats());
                }
            }
            Application application = ActivityThread.currentApplication();
            if (application != null) {
                ActivityManager am = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
                ConfigurationInfo info = am.getDeviceConfigurationInfo();
                DebugLog.i(TAG, "getDeviceConfigurationInfo: getGlEsVersion(): " + info.getGlEsVersion());
                DebugLog.i(TAG, "getDeviceConfigurationInfo: reqTouchScreen: " + Utils.getClassIntApiConstantString(Configuration.class, "TOUCHSCREEN_", info.reqTouchScreen));
                DebugLog.i(TAG, "getDeviceConfigurationInfo: reqKeyboardType: " + Utils.getClassIntApiConstantString(Configuration.class, "KEYBOARD_", info.reqKeyboardType));
                DebugLog.i(TAG, "getDeviceConfigurationInfo: reqNavigation: " + Utils.getClassIntApiConstantString(Configuration.class, "NAVIGATION_", info.reqNavigation));
                DebugLog.i(TAG, "getDeviceConfigurationInfo: reqInputFeatures: " + Utils.flagsToString(ConfigurationInfo.class, "INPUT_FEATURE", info.reqInputFeatures));
            }
        }
    }

    private Initializer() {
    }

    public static void notify(String entry) {
        DebugLog.d(TAG, "notify: Starting to call " + entry);
    }

    private static void printMap(String tag, Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet())
            DebugLog.i(TAG, tag + Arrays.asList(entry.getKey(), entry.getValue()));
    }

    private static void printAllPublicConstants(String namePrefix, Class<?> clazz) {
        for (Field f : clazz.getFields()) {
            try {
                final String name = f.getName();
                final Object value = f.get(null);
                if (value != null) {
                    String printableValue = null;
                    if (value.getClass().isArray()) {
                        Object[] arrayValue = (Object[]) value;
                        if (arrayValue.length > 0) {
                            printableValue = Arrays.toString((Object[]) value);
                        }
                    } else {
                        printableValue = value.toString();
                    }
                    final String tag = namePrefix == null ? name : namePrefix + name;
                    if (!TextUtils.isEmpty(printableValue)) {
                        DebugLog.i(TAG, (f.isAnnotationPresent(Deprecated.class) ? "! DEPRECATED " : "") +
                                Arrays.asList(tag, printableValue));
                    } else {
                        DebugLog.i(TAG, "! EMPTY " + tag);
                    }
                }
            } catch (IllegalAccessException e) {
                DebugLog.v(TAG, "printAllPublicConstants: ", e);
            }
        }
    }
}
