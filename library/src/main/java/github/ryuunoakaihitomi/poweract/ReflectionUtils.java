package github.ryuunoakaihitomi.poweract;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.os.Build;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import dalvik.system.DexFile;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

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
    @SuppressLint("NewApi") // false positive (getCodeCacheDir)
    private static void removeRestriction() {
        switch (Build.VERSION.SDK_INT) {
            case R:
                // Convert Reflection4R.class to base64.
                @SuppressWarnings("SpellCheckingInspection") final String dexBase64 = "ZGV4CjAzNQA/x80nW2uwaqMBB1vME8JR91s8IJXoSNRYBQAAcAAAAHhWNBIAAAAAAAAAAMQEAAAcAAAAcAAAAAwAAADgAAAABAAAABABAAABAAAAQAEAAAgAAABIAQAAAQAAAIgBAACwAwAAqAEAAB4DAAAoAwAAMAMAADMDAAA4AwAAPgMAAFUDAABoAwAAfAMAAJADAACkAwAAuwMAANcDAADlAwAA6AMAAOwDAAAABAAAFQQAACoEAABDBAAATAQAAF8EAABrBAAAcwQAAHYEAAB7BAAAjAQAAJUEAAAEAAAABQAAAAYAAAAHAAAACAAAAAkAAAAKAAAACwAAAA0AAAAPAAAAEAAAABEAAAADAAAAAwAAAAgDAAADAAAABwAAABADAAANAAAACAAAAAAAAAAOAAAACAAAABgDAAAFAAEAGAAAAAAAAgAAAAAAAAACAAEAAAAAAAIAFwAAAAEAAwAaAAAAAgABABQAAAADAAIAAQAAAAYAAgAZAAAABwAAABYAAAAAAAAAAQAAAAMAAAAAAAAA/////wAAAACtBAAAAAAAAAgAAAADAAEAAAAAAIkAAABiAAAAGgEMAG4gAwAQABwAAgAaARQAEiIjIgkAEgMcBAQATQQCAxITHAQJAE0EAgNuMAQAEAIMAhwAAgAaARMAEhMjMwkAEgQcBQQATQUDBG4wBAAQAwwAEgESEyMzCgASBBoFEgBNBQMEbjAHABADDAAfAAIAEiEjEQoAEgMaBBUATQQBAxITEgRNBAEDbjAHAAIBDAEfAQcAEiMjMwoAEgQaBRsATQUDBBIUEhUjVQkAEgYcBwsATQcFBk0FAwRuMAcAAgMMAB8ABwA4AR0AOAAbABICEgMjMwoAbjAHACEDDAESEiMiCgASAxIUI0QLABIFGgYCAE0GBAVNBAIDbjAHABACDgANAG4QBgAAACj7AAAHAAAAfAABAAEBBoQBAAAAAQABAAEAAAAAAAAABAAAAHAQBQAAAA4AAAAAAAAAAAAAAAAAAQAAAA4AAAACAAAAAwAKAAIAAAAEAAkAAQAAAAQACDxjbGluaXQ+AAY8aW5pdD4AAUwAA0xMTAAETFJSOwAVTGphdmEvaW8vUHJpbnRTdHJlYW07ABFMamF2YS9sYW5nL0NsYXNzOwASTGphdmEvbGFuZy9PYmplY3Q7ABJMamF2YS9sYW5nL1N0cmluZzsAEkxqYXZhL2xhbmcvU3lzdGVtOwAVTGphdmEvbGFuZy9UaHJvd2FibGU7ABpMamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kOwAMUmVmbGVjdGlvbjRSAAFWAAJWTAASW0xqYXZhL2xhbmcvQ2xhc3M7ABNbTGphdmEvbGFuZy9PYmplY3Q7ABNbTGphdmEvbGFuZy9TdHJpbmc7ABdkYWx2aWsuc3lzdGVtLlZNUnVudGltZQAHZm9yTmFtZQARZ2V0RGVjbGFyZWRNZXRob2QACmdldFJ1bnRpbWUABmludm9rZQABbAADb3V0AA9wcmludFN0YWNrVHJhY2UAB3ByaW50bG4AFnNldEhpZGRlbkFwaUV4ZW1wdGlvbnMAAAADAACIgASoAwGBgATcBQEJ9AUAAAAMAAAAAAAAAAEAAAAAAAAAAQAAABwAAABwAAAAAgAAAAwAAADgAAAAAwAAAAQAAAAQAQAABAAAAAEAAABAAQAABQAAAAgAAABIAQAABgAAAAEAAACIAQAAASAAAAMAAACoAQAAARAAAAMAAAAIAwAAAiAAABwAAAAeAwAAACAAAAEAAACtBAAAABAAAAEAAADEBAAA";
                DebugLog.v(TAG, "removeRestriction: dex length = " + dexBase64.length());
                byte[] bytes = Base64.decode(dexBase64, Base64.NO_WRAP);
                @SuppressWarnings("ConstantConditions")
                File codeCacheDir = ActivityThread.currentApplication().getCodeCacheDir();
                File code = new File(codeCacheDir, UUID.randomUUID() + ".dex");
                try (FileOutputStream fos = new FileOutputStream(code)) {
                    fos.write(bytes);
                    // DexFile will be removed in a future Android release.
                    DexFile dexFile = new DexFile(code);
                    Class<?> reflection4RClazz = dexFile.loadClass("RR", null);
                    reflection4RClazz.getDeclaredMethod("l").invoke(null);
                } catch (Throwable e) {
                    DebugLog.e(TAG, "removeRestriction: ", e);
                } finally {
                    if (code.exists()) {
                        boolean delete = code.delete();
                        DebugLog.d(TAG, "removeRestriction: delete temp dex(" + code.getName() + "): " + delete);
                    }
                }
                break;
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
