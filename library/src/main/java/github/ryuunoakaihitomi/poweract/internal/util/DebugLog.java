package github.ryuunoakaihitomi.poweract.internal.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import github.ryuunoakaihitomi.poweract.BuildConfig;

public final class DebugLog {

    private static final String MAIN_TAG = "PowerAct";
    public static boolean enabled = true;

    private DebugLog() {
    }

    public static void v(@Nullable String tag, @NonNull String msg) {
        if (enabled) Log.v(formatTag(tag), msg);
    }

    public static void v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enabled) Log.v(formatTag(tag), msg, tr);
    }

    public static void d(@Nullable String tag, @NonNull String msg) {
        if (enabled) Log.d(formatTag(tag), msg);
    }

    public static void d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enabled) Log.d(formatTag(tag), msg, tr);
    }

    public static void i(@Nullable String tag, @NonNull String msg) {
        if (enabled) Log.i(formatTag(tag), msg);
    }

    public static void i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enabled) Log.i(formatTag(tag), msg, tr);
    }

    public static void w(@Nullable String tag, @NonNull String msg) {
        if (enabled) Log.w(formatTag(tag), msg);
    }

    public static void w(@Nullable String tag, @Nullable Throwable tr) {
        if (enabled) Log.w(formatTag(tag), tr);
    }

    public static void w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enabled) Log.w(formatTag(tag), msg, tr);
    }

    public static void e(@Nullable String tag, @NonNull String msg) {
        if (enabled) Log.e(formatTag(tag), msg);
    }

    public static void e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enabled) Log.e(formatTag(tag), msg, tr);
    }

    private static String formatTag(String baseTag) {
        if (MAIN_TAG.equals(baseTag)) return baseTag;
        final int maxTagLength = 23;
        String tag = MAIN_TAG + '_' + baseTag;
        /*
         * See {@link Log#isLoggable(String, int)}
         */
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N && tag.length() > maxTagLength) {
            if (BuildConfig.DEBUG) {
                Log.w(MAIN_TAG, "Formatted tag (" + tag + ") is too long!");
            }
            tag = baseTag;
        }
        return tag;
    }
}
