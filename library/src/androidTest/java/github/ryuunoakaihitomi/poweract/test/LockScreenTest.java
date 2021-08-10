package github.ryuunoakaihitomi.poweract.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.CountDownLatch;

import github.ryuunoakaihitomi.poweract.Callback;

public final class LockScreenTest extends BaseTest {

    public static final int WAIT_TIME_MILLIS = 20_000;
    private static final String TAG = "LockScreenTest";
    private final Execution mExecution;
    private final PowerManager mPowerManager;

    public LockScreenTest(@NonNull Execution execution) {
        mExecution = execution;
        mPowerManager = (PowerManager) targetContext.getSystemService(Context.POWER_SERVICE);
    }

    public void test() throws InterruptedException {
        assertTrue("You must turn screen on before executing this case.", isInteractive());
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Callback callback = new Callback() {

            @Override
            public void done() {
                SystemClock.sleep(100);     // Wait for the complete completion of screen lock.
                assertFalse("lockScreen() doesn't work.", isInteractive());
                countDownLatch.countDown();
            }

            @Override
            public void failed() {
                fail("Callback failed() called.");
            }
        };
        assertNotNull(mExecution);
        try {
            mExecution.run(callback);
        } catch (Throwable throwable) {
            Log.w(TAG, "test: Throwable from Callback", throwable);
            fail(throwable.getMessage());
        }
        countDownLatch.await();
    }

    private boolean isInteractive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return mPowerManager.isInteractive();
        } else {
            return mPowerManager.isScreenOn();
        }
    }

    @FunctionalInterface
    public interface Execution {
        void run(Callback callback) throws Throwable;
    }
}
