package poweract.local.tool;

import java.lang.reflect.Method;

/**
 * This class isn't used to execute directly.
 * It's used to compile to dex file and load in ReflectionUtils.
 * We'd better minimize the size of final product.
 * <p>
 * Steps:
 * <p>
 * Comment the package. (first line)
 * <p>
 * <code>javac -g:none [path]</code>
 * <p>
 * copy *.class to [SDK]/build-tools
 * <p>
 * <code>./dx --dex --output=*.dex *.class</code>
 */
@SuppressWarnings("unused")
public class RR {

    static {
        System.out.println("Reflection4R");
        try {
            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

            Class<?> VMRuntimeClazz = (Class<?>) Class.class.getDeclaredMethod("forName", String.class)
                    .invoke(null, "dalvik.system.VMRuntime");
            Method getRuntime = (Method) getDeclaredMethod.invoke(VMRuntimeClazz, "getRuntime", null);
            Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(VMRuntimeClazz, "setHiddenApiExemptions", new Class[]{String[].class});
            Object sVmRuntime = getRuntime.invoke(null);
            setHiddenApiExemptions.invoke(sVmRuntime, (Object) new String[]{""});
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
