package github.ryuunoakaihitomi.poweract.internal.util;

import org.junit.Test;

import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;
import static org.junit.Assert.assertNotNull;

/**
 * Test for behavior, not in <code>androidTest</code>.
 */
public class CallbackHelperTest extends BaseTest {

    private static final int
            ACTION_DONE = 1,
            ACTION_FAILED = 2;
    private int mAction;
    private final Callback mCallback = new Callback() {

        @Override
        public void done() {
            mAction = ACTION_DONE;
        }

        @Override
        public void failed() {
            mAction = ACTION_FAILED;
        }
    };

    @Test
    public void of() {
        assertNotNull(CallbackHelper.of(() -> {
        }));
        assertNotNull(CallbackHelper.of(null));
    }

    @Test
    public void done() {
        CallbackHelper.of(mCallback).done();
        await().atMost(ONE_SECOND).until(() -> mAction == ACTION_DONE);
    }

    @Test
    public void failed() {
        CallbackHelper.of(mCallback).failed();
        await().atMost(ONE_SECOND).until(() -> mAction == ACTION_FAILED);
    }
}
