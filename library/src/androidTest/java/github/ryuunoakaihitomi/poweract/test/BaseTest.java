package github.ryuunoakaihitomi.poweract.test;

import android.content.Context;
import android.content.Intent;
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

public class BaseTest {

    protected static Context targetContext;

    @SuppressWarnings({"CanBeFinal", "WeakerAccess"}) // May be used in sub test.
    protected static boolean autoUninstall = true;

    private static final String TAG = "BaseTest";

    @BeforeClass
    public static void initialize() {
        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        targetContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
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
            Log.w(TAG, "requestUninstall: Uninstall automatically.");
            final UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            uiDevice.wakeUp();
            uiDevice.findObject(new UiSelector().text(Resources.getSystem().getString(android.R.string.ok))).click();
        }
    }
}
