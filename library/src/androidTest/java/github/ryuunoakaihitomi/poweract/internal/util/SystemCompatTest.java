package github.ryuunoakaihitomi.poweract.internal.util;

import android.os.Build;

import androidx.test.filters.FlakyTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.filters.Suppress;

import org.junit.BeforeClass;
import org.junit.Test;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O_MR1;

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
@SdkSuppress(maxSdkVersion = Build.VERSION_CODES.R)
public final class SystemCompatTest extends BaseTest {

    @BeforeClass
    public static void removeReflectionRestriction() throws ClassNotFoundException {
        // Only for loading static initializer.
        Class.forName("github.ryuunoakaihitomi.poweract.internal.ReflectionUtils");
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

    @SdkSuppress(minSdkVersion = N, maxSdkVersion = O_MR1)
    @Test(expected = SecurityException.class)
    public void rebootSafeMode() {
        SystemCompat.rebootSafeMode();
    }

    @Suppress // The case will crash the app.
    @Test(expected = SecurityException.class)
    public void execShell() {
        SystemCompat.execShell("echo test");
    }

    @Test(expected = SecurityException.class)
    public void crash() {
        SystemCompat.crash();
    }
}
