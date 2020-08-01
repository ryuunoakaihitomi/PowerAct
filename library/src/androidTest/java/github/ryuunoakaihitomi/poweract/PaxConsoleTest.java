package github.ryuunoakaihitomi.poweract;

import android.util.Log;

import androidx.test.filters.RequiresDevice;
import androidx.test.filters.SmallTest;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.junit.Assert.assertNotNull;

@SmallTest
public final class PaxConsoleTest extends BaseTest {

    private static final String TAG = "PaxConsoleTest";

    @RequiresDevice
    @Test
    public void getInterface() {
        final int threadCount = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "getInterface: CPU has " + threadCount + " cores.");
        final Runnable runnable = () -> assertNotNull(PaxConsole.getInterface());
        final ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            pool.submit(runnable);
            Log.d(TAG, "getInterface: No." + i + " runnable submitted.");
        }
        pool.shutdown();
    }
}
