package github.ryuunoakaihitomi.poweract;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
final class DebugLog {

    private static final String MAIN_TAG = "PowerAct";
    static boolean isOutput = true;

    private DebugLog() {
    }

    public static void v(@Nullable String tag, @NonNull String msg) {
        if (isOutput) Log.v(formatTag(tag), msg);
    }

    public static void v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (isOutput) Log.v(formatTag(tag), msg, tr);
    }

    public static void d(@Nullable String tag, @NonNull String msg) {
        if (isOutput) Log.d(formatTag(tag), msg);
    }

    public static void d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (isOutput) Log.d(formatTag(tag), msg, tr);
    }

    public static void i(@Nullable String tag, @NonNull String msg) {
        if (isOutput) Log.i(formatTag(tag), msg);
    }

    public static void i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (isOutput) Log.i(formatTag(tag), msg, tr);
    }

    public static void w(@Nullable String tag, @NonNull String msg) {
        if (isOutput) Log.w(formatTag(tag), msg);
    }

    public static void w(@Nullable String tag, @Nullable Throwable tr) {
        if (isOutput) Log.w(formatTag(tag), tr);
    }

    public static void w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (isOutput) Log.w(formatTag(tag), msg, tr);
    }

    public static void e(@Nullable String tag, @NonNull String msg) {
        if (isOutput) Log.e(formatTag(tag), msg);
    }

    public static void e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (isOutput) Log.e(formatTag(tag), msg, tr);
    }

    private static String formatTag(String baseTag) {
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
