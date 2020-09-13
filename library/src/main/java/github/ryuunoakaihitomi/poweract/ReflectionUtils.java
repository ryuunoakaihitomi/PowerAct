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
                // Convert compressed Reflection4R class to base64.
                @SuppressWarnings("SpellCheckingInspection") final String compressDexBase64 = "eNptlM9PE0EUx9/Mzm6hVGiHX1qb8KMHiTFAAnop0aCYGNOKKcgFDy7tAgvttmkXxOgBSEw08WS8mPA3ePJkNBxMDP+BF2M8ePDgwZMx8aDfmdkKqJv97Jv9vpk3b2Yyr+xtxccnzlP47mC6enb35e7Em+Krxx/76s8/395PnRkZsYnqRLS1MCkpep4JotNk9AT4AIRqMyK8NIVPDHYm+n+AzxOLaA/2Oyf6ATj+46AL9IJz4BKYAyvgPngKXoDXYB+8Be/BV8AwYRpkwQVwGVwHs+AO8IXJCemTQyafNtAO4qADnABdIAlSwIr4xI1VY75w068tWntn1P7GVWxG/Vrlep3KQkbbolOR2qNjGt3B92QUty+Kx6I51fMLj7I7wvy3aZ+J/BAsgTRLUDDIkXMG0dKsh+RwdridpJURggqCW7I7I9p1Kxi3KMkT3PTsJtmdnUBPkbHR07aE9lsJkkx54vCkbRl5HO0ZwEg5lE3BZ6VFL6IyxJdCWdWHswQbYDGS2db4tB4ve2Rv9hbmcjKxDirEbKcVleuoGMGlpcYobchKMCmzw2oW2ZOd6SBpp3FoBUfYZh3IhnfiHIKkOs+Rn2YPt7A3jDnbTO8ji3bKnHs9KfRpcb2HcW0FTp9F/rapUsUP/PAiOVPGWvl8nkS+WMxRb37N3XTH/NrYzYYfhHNhw3OrOUoZueIGK2NXKm6zmSN5RJpdWvNK4XENQ/1g5S/tXjP0qn9m0dr8aqN2112qeDlKH5Eb3nIFMccKXrhaK+coUTSCXwsmi8QWiC/kSS7+m1f34n8SOya2Musvu5VNf320qbMaXSgUN4LQr3oUW641brhopFa8cMYrVdyGVzaJUBxSq5/jB5u1dY+s2kZIXXWzYW5pfb7hlhBFC5WA+ppeeM0vl71guu5f3fKqdbWKpr4m9Ghb7FlsZ1sc2FFNad2Nlm3VG36k5lhH6o6gw9pj02H9ceiwBrFBM17VIZY8vOt80MRXtYkGTX99B5Omrerdb41eypo=";
                DebugLog.v(TAG, "removeRestriction: compressed dex length = " + compressDexBase64.length());
                byte[] bytes = Utils.decompress(Base64.decode(compressDexBase64, Base64.NO_WRAP));
                @SuppressWarnings("ConstantConditions")
                File codeCacheDir = ActivityThread.currentApplication().getCodeCacheDir();
                File code = new File(codeCacheDir, UUID.randomUUID() + ".dex");
                try (FileOutputStream fos = new FileOutputStream(code)) {
                    fos.write(bytes);
                    // DexFile will be removed in a future Android release.
                    // -----------------------------------------------------------------------------
                    // "Opening an oat file without a class loader. Are you using the deprecated DexFile APIs?"
                    // platform/art/runtime/oat_file_manager.cc
                    //  If the class_loader is null there's not much we can do. This happens if a dex files is loaded
                    //  // directly with DexFile APIs instead of using class loaders.
                    //  if (class_loader == nullptr) {
                    //    LOG(WARNING) << "Opening an oat file without a class loader. "
                    //                 << "Are you using the deprecated DexFile APIs?";
                    //  }
                    // -----------------------------------------------------------------------------
                    // "DexFile * is in boot class path but is not in a known location"
                    // platform/art/master/runtime/hidden_api.cc
                    //  if (class_loader.IsNull()) {
                    //    if (kIsTargetBuild && !kIsTargetLinux) {
                    //      // This is unexpected only when running on Android.
                    //      LOG(WARNING) << "DexFile " << dex_location
                    //          << " is in boot class path but is not in a known location";
                    //    }
                    //    return Domain::kPlatform;
                    //  }
                    DexFile dexFile = new DexFile(code);
                    Class<?> reflection4RClazz = dexFile.loadClass("RR", null);
                    reflection4RClazz.getConstructor().newInstance();
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
