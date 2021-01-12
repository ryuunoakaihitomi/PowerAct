package demo.power_act.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final String TAG = "Utils";

    private Utils() {
    }

    public static String timestamp2String(String format, long timestamp) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date(timestamp));
    }

    public static boolean isAndroidWearOS(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo("com.google.android.wearable.app", 0);
            Log.i(TAG, "isAndroidWearOS: App Version:" + info.versionName);
            return (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}
