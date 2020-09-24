package github.ryuunoakaihitomi.poweract.test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;

import java.util.Arrays;

import static org.junit.Assert.fail;

public final class CommonUtils {

    public static final String PKG_NAME_PACKAGE_INSTALLER = "com.android.packageinstaller";
    public static final String PKG_NAME_SETTINGS = "com.android.settings";
    private static final String TAG = "CommonUtils";

    private CommonUtils() {
    }

    public static String getStringResource(Context context, String pkgName, String resName) {
        try {
            final Resources settingsRes = context.getPackageManager().getResourcesForApplication(pkgName);
            final int identifier = settingsRes.getIdentifier(resName, "string", pkgName);
            final String value = settingsRes.getString(identifier);
            Log.i(TAG, "getStringResource: " + Arrays.asList(pkgName, resName, value));
            return value;
        } catch (PackageManager.NameNotFoundException e) {
            fail(e.getMessage());
        }
        return "";
    }
}
