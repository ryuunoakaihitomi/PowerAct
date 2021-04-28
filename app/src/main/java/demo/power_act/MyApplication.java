package demo.power_act;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import demo.power_act.util.Utils;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();

        /*
         Wear OS API 28 does not support multi Accessibility Service in one app.
         (PaService is invisible to the user. Only test service is visible.)
        */
        if (Utils.isAndroidWearOS(this)) {
            Log.d(TAG, "onCreate: Disable TestService for Wear OS.");
            getPackageManager().setComponentEnabledSetting(
                    new ComponentName(this, TestService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Log.w(TAG, "onCreate: HiddenApiBypass is enabled. If you want to test PowerAct with hidden api restriction, you know what to do.");
            HiddenApiBypass.setHiddenApiExemptions("L");
        }
    }
}
