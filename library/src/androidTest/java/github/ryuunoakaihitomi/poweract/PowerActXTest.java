package github.ryuunoakaihitomi.poweract;

import androidx.test.filters.FlakyTest;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Test;

import github.ryuunoakaihitomi.poweract.internal.pa.PaReceiver;
import github.ryuunoakaihitomi.poweract.internal.pa.PaService;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;
import github.ryuunoakaihitomi.poweract.test.BaseTest;
import github.ryuunoakaihitomi.poweract.test.LockScreenTest;

import static org.junit.Assert.assertFalse;


@FlakyTest(detail = "These cases require root privilege. You must test it manually.")
@LargeTest
public final class PowerActXTest extends BaseTest {

    @After
    public void checkExposedComponents() {
        boolean hasExposedComponents = Utils.getComponentEnabled(targetContext, PaService.class) || Utils.getComponentEnabled(targetContext, PaReceiver.class);
        assertFalse(hasExposedComponents);
    }

    @Test(timeout = LockScreenTest.WAIT_TIME_MILLIS)
    public void lockScreen() throws InterruptedException {
        LockScreenTest test = new LockScreenTest(PowerActX::lockScreen);
        test.test();
    }
}
