package github.ryuunoakaihitomi.poweract.internal.pax;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import github.ryuunoakaihitomi.poweract.test.BaseTest;

import static org.junit.Assert.assertTrue;

public class PaxExecutorTest extends BaseTest {

    @Test
    public void checkDuplicatedToken() throws IllegalAccessException {
        Set<String> tokenSet = new HashSet<>();
        for (Field field : PaxExecutor.class.getDeclaredFields()) {
            String name = field.getName();
            if (name.startsWith("TOKEN")) {
                assertTrue("Duplicated token " + Arrays.asList(name, field.get(null)),
                        tokenSet.add(String.valueOf(field.get(null))));
            }
        }
    }
}
