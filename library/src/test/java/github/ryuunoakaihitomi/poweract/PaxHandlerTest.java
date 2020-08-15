package github.ryuunoakaihitomi.poweract;

import org.junit.Test;

import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;


public class PaxHandlerTest {

    /**
     * Must less than {@link UserGuideRunnable#RELEASE_DELAY_TIME_MILLIS} in order to execute the runnable.
     */
    @Test
    public void checkUserGuideDelayTimeMillis() {
        assertThat(PaxHandler.USER_GUIDE_DELAY_TIME_MILLIS, lessThan(UserGuideRunnable.RELEASE_DELAY_TIME_MILLIS));
    }
}
