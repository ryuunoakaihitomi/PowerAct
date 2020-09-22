package github.ryuunoakaihitomi.poweract.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Arrays;

import static org.junit.Assert.fail;

public class BaseTest {

    protected static final String PKG_NAME_SETTINGS = "com.android.settings";

    protected static Context targetContext;

    @SuppressWarnings("CanBeFinal") // May be used in sub test.
    protected static boolean autoUninstall = true;
    protected static final String PKG_NAME_PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final String TAG = "BaseTest";

    protected static String getStringResource(String pkgName, String resName) {
        final Resources settingsRes;
        try {
            settingsRes = targetContext.getPackageManager().getResourcesForApplication(pkgName);
            final int identifier = settingsRes.getIdentifier(resName, "string", pkgName);
            final String value = settingsRes.getString(identifier);
            Log.i(TAG, "getStringResource: " + Arrays.asList(pkgName, resName, value));
            return value;
        } catch (PackageManager.NameNotFoundException e) {
            fail(e.getMessage());
        }
        return "";
    }

    @BeforeClass
    public static void prepareTargetContext() {
        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @AfterClass
    public static void lastOrder() throws Throwable {
        if (autoUninstall) requestUninstall();
    }

    private static void requestUninstall() throws UiObjectNotFoundException, RemoteException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE,
                Uri.fromParts("package", context.getPackageName(), null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            uiDevice.wakeUp();
            uiDevice.findObject(new UiSelector().text(Resources.getSystem().getString(android.R.string.ok))).click();
        }
    }
}
