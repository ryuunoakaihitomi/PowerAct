package github.ryuunoakaihitomi.poweract;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PowerActXTest extends BaseTest {

    private static Method[] sDeclaredMethods;
    private static int sMethodCount;

    @BeforeClass
    public static void prepareDeclaredMethods() {
        sDeclaredMethods = PowerActX.class.getDeclaredMethods();
    }

    @AfterClass
    public static void checkMethodCount() {
        final List<Method> allApiMethods = new ArrayList<>();
        for (Method m : sDeclaredMethods) if (isApiForUser(m)) allApiMethods.add(m);
        final int currentCount = allApiMethods.size();
        assertEquals("Illegal method count. Is there a new user api? ", sMethodCount, currentCount);
    }

    private static boolean isApiForUser(final Method method) {
        final int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
    }

    @Test
    public void lockScreen() {
        checkAndCount("lockScreen", false);
    }

    @Test
    public void shutdown() {
        checkAndCount("shutdown", true);
    }

    @Test
    public void reboot() {
        checkAndCount("reboot", true);
    }

    @Test
    public void recovery() {
        checkAndCount("recovery", true);
    }

    @Test
    public void bootloader() {
        checkAndCount("bootloader", true);
    }

    @Test
    public void safeMode() {
        checkAndCount("safeMode", true);
    }

    @Test
    public void softReboot() {
        checkAndCount("softReboot", false);
    }

    private void checkAndCount(final String methodName, final boolean force) {
        sMethodCount += force ? checkForceMethod(methodName) : checkMethod(methodName);
    }

    private int checkMethod(final String name) {
        final int expectedParameterLength = 2;
        final Method[] methods = checkAkinMethodCount(name, expectedParameterLength);
        for (int i = 0; i < expectedParameterLength; i++) {
            final Method m = methods[i];
            switch (m.getParameterCount()) {
                case 0:
                    i("null param");
                    break;
                case 1:
                    i("param: [Callback]");
                    assertEquals(Callback.class, m.getParameters()[0].getType());
                    break;
                default:
                    fail("Incorrect parameter count in force method.");
            }
        }
        return expectedParameterLength;
    }

    private int checkForceMethod(final String name) {
        final int expectedParameterLength = 4;
        final Method[] methods = checkAkinMethodCount(name, expectedParameterLength);
        for (int i = 0; i < expectedParameterLength; i++) {
            final Method m = methods[i];
            switch (m.getParameterCount()) {
                case 0:
                    i("null param");
                    break;
                case 1:
                    final Class<?> type = m.getParameters()[0].getType();
                    i("param(boolean -> force): " + type.getSimpleName());
                    assertThat(true, anyOf(
                            equalTo(type.equals(boolean.class)),
                            equalTo(type.equals(Callback.class))));
                    break;
                case 2:
                    i("param: [Callback, force]");
                    final Parameter[] parameters = m.getParameters();
                    assertEquals(Callback.class, parameters[0].getType());
                    assertEquals(boolean.class, parameters[1].getType());
                    break;
                default:
                    fail("Incorrect parameter count in force method.");
            }
        }
        return expectedParameterLength;
    }

    private Method[] checkAkinMethodCount(final String name, final int expectedNum) {
        int index = 0;
        final Method[] methods = new Method[expectedNum];
        for (Method m : sDeclaredMethods) {
            if (name.equals(m.getName())
                    && isApiForUser(m)) {
                if (index < expectedNum) {
                    methods[index] = m;
                    index++;
                } else {
                    fail("Incorrect method(" + name + ") count." +
                            " expected = " + expectedNum + ", count = " + (index + 1));
                }
            }
        }
        assertEquals(expectedNum, index);
        return methods;
    }
}
