package github.ryuunoakaihitomi.poweract;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

public class PaReceiver extends DeviceAdminReceiver {

    private static final String TAG = "PaReceiver";

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Log.w(TAG, "onEnabled: Useless device admin enabled after 28.");
            // >= 28 & adminActive, remove dev admin automatically. (prevent user from enabling it manually)
            getManager(context).removeActiveAdmin(new ComponentName(context, this.getClass()));
        }
    }
}
