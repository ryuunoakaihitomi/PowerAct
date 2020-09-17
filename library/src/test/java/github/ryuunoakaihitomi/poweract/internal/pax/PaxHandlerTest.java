package github.ryuunoakaihitomi.poweract.internal.pax;

import org.junit.Test;

import github.ryuunoakaihitomi.poweract.internal.util.UserGuideRunnable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;


public class PaxHandlerTest {

    /**
     * Must less than {@link UserGuideRunnable#RELEASE_DELAY_TIME_MILLIS} in order to execute the runnable.
     */
    @Test
    public void checkUserGuideDelayTimeMillis() {
        assertThat(PaxHandler.USER_GUIDE_DELAY_TIME_MILLIS, lessThan(UserGuideRunnable.RELEASE_DELAY_TIME_MILLIS));
    }
}
