package github.ryuunoakaihitomi.poweract.test;

import static org.junit.Assert.fail;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;

import androidx.test.uiautomator.UiDevice;

import java.io.IOException;
import java.util.Arrays;

public final class CommonUtils {

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

    public static void dumpWindowHierarchy2Stdout(UiDevice device) {
        try {
            device.dumpWindowHierarchy(System.out);
        } catch (IOException ignore) {
        }
    }
}
