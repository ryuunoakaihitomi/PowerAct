package github.ryuunoakaihitomi.poweract.internal.util;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DebugUtils;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import github.ryuunoakaihitomi.poweract.BuildConfig;

/**
 * Internal unclassified utilities
 */
public class Utils {

    private static final String TAG = "Utils";

    private Utils() {
    }

    /**
     * Developer Note:
     * <p>
     * <b>
     * DO NOT USE {@link AccessibilityManager#isEnabled()}!!!
     * It will return true when the other apps enable their {@link AccessibilityService}.
     * DO NOT USE {@link AccessibilityManager#getEnabledAccessibilityServiceList(int)} AND {@link AccessibilityServiceInfo#FEEDBACK_ALL_MASK}!!!
     * It will ignore the services with undefined feedback types.
     * </b>
     *
     * @param context              {@link Context}
     * @param accessibilityService {@link AccessibilityService}
     * @return <a href="https://stackoverflow.com/a/40568194">How do you check if a particular AccessibilityService is enabled?</a>
     */
    @SuppressWarnings({"SameParameterValue"})
    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);
        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) return false;
        DebugLog.d(TAG, "isAccessibilityServiceEnabled: Enabled services are " + enabledServicesSetting);
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);
            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }
        return false;
    }

    @WorkerThread
    public static synchronized boolean runSuJavaWithAppProcess(@NonNull Context context, @NonNull Class<?> cls, @NonNull String... args) {
        final long start = SystemClock.elapsedRealtime();
        final String packageResourcePath = context.getPackageResourcePath();
        final String className = cls.getName();
        final String argLine = TextUtils.join(" ", args);
//        final String cmdDir = "/system/bin";
        final String cmdDir = File.separator;
        final String
                shellExportClassPath = "export CLASSPATH=" + packageResourcePath,
                shellExecAppProcess = "app_process " + cmdDir + " " + className + " " + argLine;
        DebugLog.i(TAG, "runSuJavaWithAppProcess: commands " + Arrays.asList(shellExportClassPath, shellExecAppProcess));
        if (LibraryCompat.isLibsuAvailable()) {
            final Shell.Result result = Shell.su(shellExportClassPath, shellExecAppProcess).exec();
            final boolean success = result.isSuccess();
            if (BuildConfig.DEBUG) {
                // It's weird, can't get stderr.
                DebugLog.d(TAG, "runSuJavaWithAppProcess: result from libsu: code = "
                        + result.getCode() + ", err = " + result.getErr() + ", out = " + result.getOut());
            }
            DebugLog.i(TAG, "runSuJavaWithAppProcess: success = " + success +
                    ", blocked in " + (SystemClock.elapsedRealtime() - start) + " ms.");
            return success;
        }

        DebugLog.w(TAG, "runSuJavaWithAppProcess: Use legacy solution. Please import libsu to get better performance.");
        Process suProcess = null;
        int exitCode = Integer.MIN_VALUE;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream stream = new DataOutputStream(suProcess.getOutputStream());
            stream.writeBytes(shellExportClassPath + '\n');
            stream.writeBytes(shellExecAppProcess + '\n');
            stream.writeBytes("exit\n");
            stream.flush();
        } catch (IOException e) {
            DebugLog.e(TAG, "runSuJavaWithAppProcess: " +
                    "blocked in " + (SystemClock.elapsedRealtime() - start) + " ms.", e);
            return false;
        } finally {
            if (suProcess != null) {
                try (InputStream errorStream = suProcess.getErrorStream();
                     InputStreamReader isr = new InputStreamReader(errorStream);
                     BufferedReader br = new BufferedReader(isr)) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    String stderr = sb.toString();
                    if (!TextUtils.isEmpty(stderr)) {
                        // "Killed" means "An exception threw out from privileged class".
                        DebugLog.i(TAG, "runSuJavaWithAppProcess: stderr = " + stderr);
                    }
                    exitCode = suProcess.waitFor();
                } catch (InterruptedException | IOException e) {
                    DebugLog.e(TAG, "runSuJavaWithAppProcess: finally", e);
                }
            }
        }
        DebugLog.i(TAG, "runSuJavaWithAppProcess: exit code = " + exitCode +
                ", blocked in " + (SystemClock.elapsedRealtime() - start) + " ms.");
        return exitCode == 0;
    }

    public static void setComponentEnabled(Context context, Class<?> componentClass, boolean enabled) {
        if (getComponentEnabled(context, componentClass) != enabled) {
            DebugLog.d(TAG, "setComponentEnabled: Update state: " + Arrays.asList(componentClass.getSimpleName(), enabled));
            PackageManager pm = context.getPackageManager();
            ComponentName cn = new ComponentName(context, componentClass);
            pm.setComponentEnabledSetting(cn,
                    enabled ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    public static boolean getComponentEnabled(Context context, Class<?> componentClass) {
        final int state = context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, componentClass));
        return state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
    }

    public static SparseArray<String> getClassIntApiConstant(@NonNull Class<?> clz, @NonNull String prefix) {
        SparseArray<String> container = new SparseArray<>();
        for (Field field : clz.getDeclaredFields()) {
            String name = field.getName();
            final int modifiers = field.getModifiers();
            if (name.startsWith(prefix) &&
                    /* check modifiers */
                    Modifier.isStatic(modifiers) &&
                    Modifier.isFinal(modifiers) &&
                    // check type
                    field.getType().equals(int.class)) {
                try {
                    container.put(field.getInt(null), name);
                } catch (IllegalAccessException e) {
                    DebugLog.w(TAG, "getClassIntConstant: name = " + name, e);
                }
            }
        }
        if (BuildConfig.DEBUG) {
            DebugLog.d(TAG, "getClassIntApiConstant() called with: clz = [" + clz + "], prefix = [" + prefix + "]");
            for (int i = 0; i < container.size(); i++) {
                DebugLog.d(TAG, "getClassIntApiConstant: " +
                        Arrays.asList(i, container.valueAt(i), container.keyAt(i)));
            }
        }
        return container;
    }

    public static String getClassIntApiConstantString(@NonNull Class<?> clz, @NonNull String prefix, int value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String ret = (String) ReflectionUtils.invokeStaticMethod(ReflectionUtils.findMethod(
                    DebugUtils.class, "valueToString", Class.class, String.class, Integer.TYPE),
                    clz, prefix, value);
            if (ret != null && !String.valueOf(value).equals(ret)) return prefix + ret;
            DebugLog.d(TAG, "getClassIntApiConstantString: Use alternative solution. ret=" + ret);
        }
        return getClassIntApiConstant(clz, prefix).get(value, String.valueOf(value));
    }

    /**
     * Use prefixed constants (static final values) on given class to turn flags
     * into human-readable string.
     *
     * @see DebugUtils flagsToString(Class, String, int)
     */
    public static String flagsToString(Class<?> clazz, String prefix, int flags) {
        final StringBuilder res = new StringBuilder();
        boolean flagsWasZero = flags == 0;

        for (Field field : clazz.getDeclaredFields()) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && field.getType().equals(int.class) && field.getName().startsWith(prefix)) {
                try {
                    final int value = field.getInt(null);
                    if (value == 0 && flagsWasZero) {
                        return field.getName();
                    }
                    if (value != 0 && (flags & value) == value) {
                        flags &= ~value;
                        res.append(field.getName()).append('|');
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        if (flags != 0 || res.length() == 0) {
            res.append(Integer.toHexString(flags));
        } else {
            res.deleteCharAt(res.length() - 1);
        }
        return res.toString();
    }

    @AnyThread
    public static boolean isMainThread() {
        Looper mainLooper = Looper.getMainLooper();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                mainLooper.isCurrentThread() :
                Looper.myLooper() == mainLooper;
    }

    public static int randomNonZero() {
        int ret = new Random().nextInt();
        return ret == 0 ? randomNonZero() : ret;
    }

    public static int randomNaturalNumber() {
        return Math.abs(new Random().nextInt());
    }

    public static Bitmap makeTintBitmap(@NonNull Bitmap inputBitmap, @ColorInt int tintColor) {
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getWidth(), inputBitmap.getHeight(), inputBitmap.getConfig());
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inputBitmap, 0, 0, paint);
        return outputBitmap;
    }

    /**
     * @param context {@link Context}
     * @return Check if is running in the work profile.
     * @author <a href="https://stackoverflow.com/a/33801131">earlypearl</a>
     */
    public static boolean isInWorkProfile(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            List<ComponentName> activeAdmins = dpm.getActiveAdmins();
            if (activeAdmins != null) {
                for (ComponentName admin : activeAdmins) {
                    String packageName = admin.getPackageName();
                    if (dpm.isProfileOwnerApp(packageName)) {
                        DebugLog.i(TAG, "isInWorkProfile: Managed by " + packageName);
                        // It can be deceived by the other profile owners. (dpm set-profile-owner [component])
                        //return true;
                        // So we must ensure the current user is NOT system user.
                        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            return !userManager.isSystemUser();
                        } else {
                            // The owner cannot be changed or removed.
                            return userManager.getSerialNumberForUser(android.os.Process.myUserHandle()) > 0;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Avoid {@link ArrayIndexOutOfBoundsException}.
     */
    public static <T> T arraySafeGet(@NonNull T[] array, @IntRange(from = 0) final int index, T defaultValue) {
        return array.length >= index + 1 ? array[index] : defaultValue;
    }
}
