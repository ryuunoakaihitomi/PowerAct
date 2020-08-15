package github.ryuunoakaihitomi.poweract;

import android.os.Build;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

class ReflectionUtils {

    private static final String TAG = "ReflectionUtils";

    static {
        removeRestriction();
    }

    private ReflectionUtils() {
    }

    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Method findMethod(String clazzName, String methodName, Class<?>... paramTypes) {
        return findMethod(findClass(clazzName), methodName, paramTypes);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        if (clazz == null) return null;
        Method method;
        try {
            method = clazz.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(name, paramTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
        setAccessible(method);
        return method;
    }

    public static Object invokeStaticMethod(Method method, Object... args) {
        return invokeMethod(method, null, args);
    }

    public static Object invokeMethod(Method method, Object instance, Object... args) {
        if (method == null) return null;
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
        DebugLog.w(TAG, "invokeMethod: " + method + " failed to invoke.");
        return null;
    }

    public static Method findMethodCallerSensitive(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> clsClz = Class.class;
        Method
                getDeclaredMethod = findMethod(clsClz, "getDeclaredMethod", String.class, Class[].class),
                getMethod = findMethod(clsClz, "getMethod", String.class, Class[].class),
                method;
        if (getDeclaredMethod == null || getMethod == null) {
            DebugLog.e(TAG, "findMethodCallerSensitive: Cannon reflect java.lang.reflect.*");
            return null;
        }
        method = (Method) invokeMethod(getDeclaredMethod, clazz, name, paramTypes);
        if (method == null) {
            method = (Method) invokeMethod(getMethod, clazz, name, paramTypes);
        }
        setAccessible(method);
        return method;
    }

    public static Method findMethodCallerSensitive(String clazzName, String methodName, Class<?>... paramTypes) {
        return findMethodCallerSensitive(findClass(clazzName), methodName, paramTypes);
    }

    private static void setAccessible(AccessibleObject accessibleObject) {
        if (accessibleObject != null && !accessibleObject.isAccessible()) {
            if (BuildConfig.DEBUG)
                DebugLog.i(TAG, "setAccessible: [" + accessibleObject + "] is not accessible");
            accessibleObject.setAccessible(true);
        }
    }

    /**
     * @see <a href="https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces">Restrictions on non-SDK interfaces</a>
     * @see <a href="https://github.com/tiann/FreeReflection/">FreeReflection</a>
     */
    private static void removeRestriction() {
        switch (Build.VERSION.SDK_INT) {
            case Q:
            case P:
                String vmRuntimeClassName = "dalvik.system.VMRuntime";
                Object runtime = invokeStaticMethod(findMethod(vmRuntimeClassName, "getRuntime"));
                invokeMethod(findMethodCallerSensitive(
                        vmRuntimeClassName, "setHiddenApiExemptions", String[].class),
                        runtime, (Object) new String[]{"L"});
                break;
            default:
                DebugLog.v(TAG, "removeRestriction: no-op");
        }
    }
}
