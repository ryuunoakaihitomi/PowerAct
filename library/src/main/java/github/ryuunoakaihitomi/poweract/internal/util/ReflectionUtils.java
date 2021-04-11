package github.ryuunoakaihitomi.poweract.internal.util;

import android.os.Build;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import github.ryuunoakaihitomi.poweract.BuildConfig;

public class ReflectionUtils {

    private static final String TAG = "ReflectionUtils";

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions("L");
        }
    }

    private ReflectionUtils() {
    }

    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        } /* Unknown env-specific throwable */ catch (Throwable t) {
            DebugLog.d(TAG, "findClass: ", t);
            return null;
        }
    }

    public static boolean allClassesExist(String... classNames) {
        boolean checked = false;
        for (String className : classNames) {
            if (!checked) checked = true;
            if (findClass(className) == null) {
                DebugLog.d(TAG, "allClassesExist: " + className + " not exists!");
                return false;
            }
        }
        return checked;
    }

    public static Field findField(Class<?> clazz, String name) {
        if (clazz == null) return null;
        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(name);
            } catch (NoSuchFieldException ex) {
                return null;
            }
        }
        setAccessible(field);
        return field;
    }

    public static Object fetchField(Field field, Object instance) {
        if (field == null) return null;
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static void setField(Field field, Object instance, Object value) {
        if (field == null) return;
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            DebugLog.e(TAG, "setFieldNullInstance: ", e);
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

    private static void setAccessible(AccessibleObject accessibleObject) {
        if (accessibleObject != null && !accessibleObject.isAccessible()) {
            if (BuildConfig.DEBUG)
                DebugLog.i(TAG, "setAccessible: [" + accessibleObject + "] is not accessible");
            accessibleObject.setAccessible(true);
        }
    }
}
