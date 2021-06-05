package demo.power_act.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private Utils() {
    }

    public static String timestamp2String(String format, long timestamp) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date(timestamp));
    }

    public static boolean isAndroidWearOS(Context context) {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.KITKAT_WATCH) {
            UiModeManager uiModeManager = sdk >= Build.VERSION_CODES.M
                    ? context.getSystemService(UiModeManager.class)
                    : (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_WATCH;
        }
        return false;
//        try {
//            PackageManager pm = context.getPackageManager();
//            PackageInfo info = pm.getPackageInfo("com.google.android.wearable.app", 0);
//            Log.i(TAG, "isAndroidWearOS: App Version:" + info.versionName);
//            return (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
//        } catch (PackageManager.NameNotFoundException ignored) {
//            return false;
//        }
    }
}
