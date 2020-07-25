package github.ryuunoakaihitomi.poweract;

import androidx.test.filters.SdkSuppress;
import androidx.test.filters.Suppress;

import org.junit.Test;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;

/**
 * Not for regular test.
 * <p>
 * It's used to check whether there're corresponding APIs in the current env.
 * Since {@link android.os.Build.VERSION_CODES#P},
 * remove {@link SdkSuppress#maxSdkVersion()} and see logcat: Accessing hidden method...
 */
@SdkSuppress(maxSdkVersion = O_MR1)
public class PaxCompatTest {

    @Test(expected = SecurityException.class)
    public void goToSleep() {
        PaxCompat.goToSleep();
    }

    @Test(expected = SecurityException.class)
    public void reboot() {
        PaxCompat.reboot(null);
    }

    @Test(expected = SecurityException.class)
    public void shutdown() {
        PaxCompat.shutdown();
    }

    @SdkSuppress(minSdkVersion = M, maxSdkVersion = O_MR1)
    @Test(expected = SecurityException.class)
    public void rebootSafeMode() {
        PaxCompat.rebootSafeMode();
    }

    @Suppress // The case will crash the app.
    @Test(expected = SecurityException.class)
    public void execShell() {
        PaxCompat.execShell("echo test");
    }

    @Test(expected = SecurityException.class)
    public void crash() {
        PaxCompat.crash();
    }
}
