package github.ryuunoakaihitomi.poweract;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PaxInterfaceTest extends BaseTest {

    private final Method[] methods = PaxInterface.class.getDeclaredMethods();

    @Test
    public void A$checkExecutorApiAnnotation() {
        for (Method method : methods) {
            boolean hasAnnotation = false;
            for (Annotation annotation : method.getDeclaredAnnotations())
                if (annotation.annotationType().equals(PaxExecApi.class)) hasAnnotation = true;
            assertTrue("Method " + method.getName() + " does not have a @PaxExecApi annotation.",
                    hasAnnotation);
        }
    }

    @Test
    public void checkParameter() {
        for (Method method : methods) {
            final int count = method.getParameterCount();
            final Parameter[] parameters = method.getParameters();
            switch (count) {
                case 2:
                    /* force */
                    assertEquals(boolean.class, parameters[1].getType());
                    assertThat(parameters[1].getName(), is("force"));
                    i(method.getName() + "() has force mode.");
                case 1:
                    assertEquals(Callback.class, parameters[0].getType());
                    break;
                default:
                    fail("Illegal parameter count: " + Arrays.asList(parameters) + " = " + count);
            }
        }
    }

    @Test
    public void B$checkDuplicatedValue() {
        Set<String> valueSet = new HashSet<>();
        for (Method method : methods) {
            PaxExecApi annotation = method.getDeclaredAnnotation(PaxExecApi.class);
            assertNotNull(annotation);
            String value = annotation.value();
            assertTrue("Method " + method.getName() + " has duplicated value of @PaxExecApi: " + value, valueSet.add(value));
        }
    }
}
