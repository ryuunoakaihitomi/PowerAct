package github.ryuunoakaihitomi.poweract;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

/**
 * <b>NOT FOR EXTERNAL USE!</b> It has to be public in order for system to call.
 * <p>
 * The {@link DeviceAdminReceiver} of PowerAct.
 * To provide the permission to lock screen before 28 and reboot since 24.
 *
 * @see DevicePolicyManager#lockNow()
 * @see <a href="https://developer.android.com/guide/topics/admin/device-admin">Device administration overview</a>
 */
public final class PaReceiver extends DeviceAdminReceiver {

    private static final String TAG = "PaReceiver";

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DevicePolicyManager manager = getManager(context);
            DebugLog.w(TAG, "onEnabled: The device admin enabled after 28." +
                    " isDeviceOwner=" + manager.isDeviceOwnerApp(context.getPackageName()));
            // >= 28 & adminActive, remove dev admin automatically. (prevent user from enabling it manually)
            // The result of isDeviceOwnerApp() is uncertain here,
            // and removeActiveAdmin() don't effect on the device owner, no need to judge.
            manager.removeActiveAdmin(getWho(context));
        }
    }
}
