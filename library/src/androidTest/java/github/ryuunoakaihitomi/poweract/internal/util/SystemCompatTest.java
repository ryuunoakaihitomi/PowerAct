package github.ryuunoakaihitomi.poweract.internal.util;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.S;

import android.content.Context;
import android.os.ServiceManager;
import android.util.Log;

import androidx.test.filters.FlakyTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.filters.Suppress;

import org.junit.BeforeClass;
import org.junit.Test;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

/**
 * Not for regular test.
 * <p>
 * It's used to check whether there're corresponding APIs in the current env.
 * Since {@link android.os.Build.VERSION_CODES#P},
 * remove {@link SdkSuppress#maxSdkVersion()} and see logcat: Accessing hidden method...
 */
@FlakyTest(detail = "Only for testing available hidden API.")
@SmallTest
//@SdkSuppress(maxSdkVersion = O_MR1)
@SdkSuppress(maxSdkVersion = S)
public final class SystemCompatTest extends BaseTest {

    private static final String TAG = "SystemCompatTest";

    @BeforeClass
    public static void enforceReflectionRestrictionBypass() {
        // Bug: Arctic Fox 2020.3.1 Patch 2 will terminate all test() SILENTLY.
        // We cannot throw any exceptions here.
        //assertFalse(ReflectionUtils.hasHiddenApiRestriction());
        if (ReflectionUtils.hasHiddenApiRestriction()) {
            Log.e(TAG, "enforceReflectionRestrictionBypass");
        }
    }

    @Test(expected = SecurityException.class)
    public void goToSleep() {
        // Accessing hidden method Landroid/os/IPowerManager;->goToSleep(JII)V (greylist, linking, allowed)
        SystemCompat.goToSleep();
    }

    @Test(expected = SecurityException.class)
    public void reboot() {
        SystemCompat.reboot(null);
    }

    @Test(expected = SecurityException.class)
    public void shutdown() {
        SystemCompat.shutdown();
    }

    @SdkSuppress(minSdkVersion = N, maxSdkVersion = S)
    @Test(expected = SecurityException.class)
    public void rebootSafeMode() {
        SystemCompat.rebootSafeMode();
    }

    @Suppress // The case will crash the app.
    @Test(expected = SecurityException.class)
    public void execShell() {
        SystemCompat.execShell("echo test");
    }

    @Test(expected = NullPointerException.class)
    public void crash() {
        SystemCompat.setPowerBinder(null);
        SystemCompat.crash();
    }

    @Test(expected = SecurityException.class)
    public void crash2() {
        SystemCompat.setPowerBinder(ServiceManager.getService(Context.POWER_SERVICE));
        SystemCompat.crash();
    }
}
