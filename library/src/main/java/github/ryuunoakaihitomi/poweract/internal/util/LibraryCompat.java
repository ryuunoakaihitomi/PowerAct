package github.ryuunoakaihitomi.poweract.internal.util;

import android.os.Build;

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
        if (BuildConfig.DEBUG && isShizukuAvailable()) {
            int serverVersion = Integer.MIN_VALUE;
            try {
                serverVersion = Shizuku.getVersion();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            DebugLog.i(TAG, "static initializer: Shizuku version: " +
                    "client = " + ShizukuApiConstants.SERVER_VERSION + ", server = " + serverVersion);
        }
    }

    private LibraryCompat() {
    }

    public static boolean isLibsuAvailable() {
        return ReflectionUtils.allClassesExist("com.topjohnwu.superuser.Shell");
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
