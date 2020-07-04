package github.ryuunoakaihitomi.poweract;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

/**
 * <b>NOT FOR EXTERNAL USE!</b> It has to be public in order for system to call.
 * <p>
 * The {@link DeviceAdminReceiver} of PowerAct.
 * To provide the permission to lock screen before 28.
 * Since 28, it will be disabled automatically.
 *
 * @see DevicePolicyManager#lockNow()
 * @see <a href="https://developer.android.com/guide/topics/admin/device-admin">Device administration overview</a>
 */
public final class PaReceiver extends DeviceAdminReceiver {

    private static final String TAG = "PaReceiver";

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DebugLog.w(TAG, "onEnabled: Useless device admin enabled after 28.");
            // >= 28 & adminActive, remove dev admin automatically. (prevent user from enabling it manually)
            getManager(context).removeActiveAdmin(new ComponentName(context, this.getClass()));
            Utils.setComponentEnabled(context, this.getClass(), false);
        }
    }
}
