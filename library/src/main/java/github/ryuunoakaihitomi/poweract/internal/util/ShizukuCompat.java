package github.ryuunoakaihitomi.poweract.internal.util;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuProvider;

@RequiresApi(api = Build.VERSION_CODES.M)
public final class ShizukuCompat {

    private static final String TAG = "ShizukuCompat";

    static {
        if (Shizuku.isPreV11()) {
            DebugLog.w(TAG, "static initializer: Shizuku service should be upgraded to V11." +
                    " No longer compatible with older service.");
        }
    }

    private ShizukuCompat() {
    }

    public static int checkSelfPermission(Context context) {
        if (Shizuku.isPreV11()) {
            return context.checkSelfPermission(ShizukuProvider.PERMISSION);
        } else {
            return Shizuku.checkSelfPermission();
        }
    }

    public static void requestPermission(Fragment f, int requestCode) {
        if (Shizuku.isPreV11()) {
            f.requestPermissions(new String[]{ShizukuProvider.PERMISSION}, requestCode);
        } else {
            Shizuku.requestPermission(requestCode);
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Fragment f) {
        if (Shizuku.isPreV11()) {
            return f.shouldShowRequestPermissionRationale(ShizukuProvider.PERMISSION);
        } else {
            return Shizuku.shouldShowRequestPermissionRationale();
        }
    }
}
