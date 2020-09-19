package github.ryuunoakaihitomi.poweract;

import org.junit.Test;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExternalUtilsTest extends BaseTest {

    @Test
    public void isExposedComponentAvailable() {
        assertTrue(ExternalUtils.isExposedComponentAvailable(targetContext));
        ExternalUtils.disableExposedComponents(targetContext);
        assertFalse(ExternalUtils.isExposedComponentAvailable(targetContext));
    }
}
