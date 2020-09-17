package github.ryuunoakaihitomi.poweract.internal.util;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import github.ryuunoakaihitomi.poweract.BuildConfig;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuProvider;
import moe.shizuku.api.ShizukuService;

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
                serverVersion = ShizukuService.getVersion();
            } catch (RemoteException | IllegalStateException | SecurityException e) {
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
                        "moe.shizuku.api.ShizukuProvider",
                        "moe.shizuku.api.ShizukuBinderWrapper",
                        "moe.shizuku.api.SystemServiceHelper");
    }

    public static boolean isShizukuPrepared(@NonNull Context context) {
        return isShizukuAvailable() && ShizukuProvider.isShizukuInstalled(context) && ShizukuService.pingBinder();
    }
}
