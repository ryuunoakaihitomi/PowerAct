package github.ryuunoakaihitomi.poweract;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

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
        assertEquals("Illegal method count. Are there any new public APIs? ", sMethodCount, currentCount);
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
    public void customReboot() {
        checkAndCount("customReboot", true);
    }

    @Test
    public void safeMode() {
        checkAndCount("safeMode", true);
    }

    @Test
    public void softReboot() {
        checkAndCount("softReboot", false);
    }

    @Test
    public void restartSystemUi() {
        checkAndCount("restartSystemUi", false);
    }

    private void assertCustomReboot(String methodName) {
        i("!!! Checking customReboot() as an exception");
        assertEquals("customReboot", methodName);
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
                    final Class<?> type = m.getParameters()[0].getType();
                    i("param: [Callback] or [arg] " + type.getSimpleName());
                    assertThat(type, anyOf(is(Callback.class), is(String.class)));
                    if (type.equals(String.class)) assertCustomReboot(name);
                    break;
                case 2:
                    i("param: [arg, Callback]");
                    assertEquals(Callback.class, m.getParameters()[0].getType());
                    assertEquals(String.class, m.getParameters()[1].getType());
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
                    i("param(boolean -> force, String -> arg, Callback): " + type.getSimpleName());
                    assertThat(true, anyOf(
                            equalTo(type.equals(boolean.class)),
                            equalTo(type.equals(Callback.class)),
                            equalTo(type.equals(String.class)))
                    );
                    break;
                case 2:
                    i("param: CB, SB, SC");
                    final Parameter[] parameters = m.getParameters();
                    Class<?>[] types = new Class<?>[]{parameters[0].getType(), parameters[1].getType()};
                    Class<?>[] majority = new Class<?>[]{Callback.class, boolean.class};
                    assertThat(types, anyOf(
                            is(majority),
                            is(new Class<?>[]{String.class, boolean.class}),
                            is(new Class<?>[]{String.class, Callback.class})));
                    i("! REPORT: " + Arrays.toString(types));
                    if (!Arrays.equals(types, majority)) assertCustomReboot(name);
                    break;
                case 3:
                    assertCustomReboot(name);
                    i("param: [arg, Callback, force]");
                    List<Class<?>> assertTypes = Arrays.asList(String.class, Callback.class, boolean.class);
                    for (int j = 0; j < m.getParameterCount(); j++) {
                        assertEquals(assertTypes.get(j), m.getParameters()[j].getType());
                    }
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
