package github.ryuunoakaihitomi.poweract;

import android.util.Log;

import androidx.test.filters.RequiresDevice;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;

public class PaxConsoleTest {

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
