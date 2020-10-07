package github.ryuunoakaihitomi.poweract.internal.util;

import android.content.Context;
import android.util.Base64;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.test.annotation.UiThreadTest;
import androidx.test.filters.SmallTest;

import org.junit.Test;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SmallTest
public final class UtilsTest extends BaseTest {

    @Test
    public void getClassIntApiConstant() throws NoSuchFieldException, IllegalAccessException {
        final Class<?> testClazz = Toast.class;
        final String prefix = "LENGTH";
        final SparseArray<String> constants = Utils.getClassIntApiConstant(testClazz, prefix);
        for (int i = 0; i < constants.size(); i++) {
            final String value = constants.valueAt(i);
            assertTrue(value.startsWith(prefix));
            assertEquals(constants.keyAt(i), testClazz.getDeclaredField(value).getInt(null));
        }
    }

    @Test
    public void getClassIntApiConstantString() {
        assertEquals("MODE_PRIVATE", Utils.getClassIntApiConstantString(Context.class, "MODE", 0));
    }

    /**
     * @see <a href="https://developer.android.com/training/testing/fundamentals#on-device-unit-tests">Instrumented unit tests</a>
     */
    @UiThreadTest
    @Test
    public void isMainThread() {
        assertTrue(Utils.isMainThread());
        new Thread(() -> assertFalse(Utils.isMainThread())).start();
    }

    @Test
    public void decompress() {
        final String
                source = "With an all-new design that looks great on macOS Big Sur, " +
                "Xcode 12 has customizable font sizes for the navigator, streamlined code completion, and new document tabs. " +
                "Xcode 12 builds Universal apps by default to support Mac with Apple Silicon, often without changing a single line of code.",
                base64 = "eNpFj0FOxEAMBL/SD1hWgh/AHXGIEFydiTOxmNhR7NkV+3qcXLi15Ha5/CWxgBTU2pPyHRO7VE" +
                        "UsFGhmP466c2ZTrFQ+BrxJxdD3C76LTYznFyzkKN3DVnnQ2BizacDlwZ5xTxZD6SaVwnLPI4FrE" +
                        "+UJJ6LYujUOMb2kyYRTw0pfOTFBo1//b41d2uT4VLnx7tRA2+YYf9N7pt6yb/C+bbYH3qngfrz3" +
                        "uiUfgzQpxw2bg/WcWA+UhbSKVlAqa83ioZalU+76BweRZzk=";
        assertArrayEquals(source.getBytes(), Utils.decompress(Base64.decode(base64, Base64.NO_WRAP)));
    }

    @Test
    public void isInWorkProfile() {
        // Keep it false in most cases.
        assertFalse(Utils.isInWorkProfile(targetContext));
    }
}
