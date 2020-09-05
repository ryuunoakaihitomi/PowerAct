package github.ryuunoakaihitomi.poweract;

import android.util.Log;

import androidx.test.filters.LargeTest;

import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Duration;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public final class UserGuideRunnableTest extends BaseTest {

    private static final String TAG = "UserGuideRunnableTest";

    private static Runnable getRunnable() {
        try {
            final Field userGuideRunnable = UserGuideRunnable.class.getDeclaredField("sUserGuideRunnable");
            userGuideRunnable.setAccessible(true);
            return (Runnable) userGuideRunnable.get(null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        fail("Out of reflection!");
        return null;
    }

    @LargeTest
    @Test
    public void set() {
        final Runnable r = () -> {
        };
        UserGuideRunnable.set(r);
        assertSame(r, getRunnable());
        Log.d(TAG, "set: START");
        await().pollDelay(Duration.ofMillis(UserGuideRunnable.RELEASE_DELAY_TIME_MILLIS)).until(() -> true);
        Log.d(TAG, "set: END");
        assertNull(getRunnable());
    }

    @Test
    public void run() {
        final boolean[] b = {false};
        final Runnable r = () -> b[0] = true;
        UserGuideRunnable.set(r);
        UserGuideRunnable.run();
        await().atMost(ONE_SECOND).until(() -> b[0]);
    }

    @Test
    public void release() {
        UserGuideRunnable.set(() -> {
        });
        UserGuideRunnable.release();
        assertNull(getRunnable());
    }
}
