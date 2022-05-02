package github.ryuunoakaihitomi.poweract.internal.util;

import android.os.Build;
import android.os.Process;

import github.ryuunoakaihitomi.poweract.BuildConfig;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuApiConstants;

/**
 * Developer Note:
 * Must keep library entries in proguard.
 */
public final class LibraryCompat {

    private static final String TAG = "LibraryCompat";

    static {
        if (BuildConfig.DEBUG && isShizukuAvailable()
                /*
                 * Cannot get main handler on root uid!
                 *
                 * Caused by: java.lang.NullPointerException: Attempt to read from field 'android.os.MessageQueue android.os.Looper.mQueue' on a null object reference
                 *    at android.os.Handler.<init>(Handler.java:257)
                 *    at android.os.Handler.<init>(Handler.java:162)
                 *    at rikka.shizuku.Shizuku.<clinit>(Shizuku.java:155)
                 */
                && Process.myUid() != Process.ROOT_UID) {
            int serverVersion = Integer.MIN_VALUE;
            DebugLog.d(TAG, "static initializer: uid = " + Process.myUid());
            try {
                serverVersion = Shizuku.getVersion();
            } catch (Exception e) {
                DebugLog.e(TAG, "static initializer", e);
            }
            DebugLog.i(TAG, "static initializer: Shizuku version: " +
                    "client = " + ShizukuApiConstants.SERVER_VERSION + ", server = " + serverVersion);
        }
    }

    private LibraryCompat() {
    }

    public static boolean isLibsuAvailable() {
        // https://github.com/topjohnwu/libsu/commit/9f2fff609552bf8ef1e2d1631ee07750441c4661
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                ReflectionUtils.allClassesExist("com.topjohnwu.superuser.Shell");
    }

    public static boolean isShizukuAvailable() {
        // minSdkVersion: 23
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ReflectionUtils.allClassesExist(
                        "rikka.shizuku.ShizukuProvider",
                        "rikka.shizuku.ShizukuBinderWrapper",
                        "rikka.shizuku.SystemServiceHelper");
    }

    public static boolean isShizukuPrepared() {
        return isShizukuAvailable() && Shizuku.pingBinder();
    }
}
