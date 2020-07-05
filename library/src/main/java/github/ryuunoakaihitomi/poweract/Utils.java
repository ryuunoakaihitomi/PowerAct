package github.ryuunoakaihitomi.poweract;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

class Utils {

    private static final String TAG = "Utils";

    private Utils() {
    }

    /**
     * Developer Note:
     * <p>
     * <b>
     * DO NOT USE {@link AccessibilityManager#isEnabled()}!!!
     * It will return true when the other apps enable their {@link AccessibilityService}.
     * </b>
     *
     * @param context              {@link Context}
     * @param accessibilityService {@link AccessibilityService}
     * @return <a href="https://stackoverflow.com/a/40568194">How do you check if a particular AccessibilityService is enabled?</a>
     */
    @SuppressWarnings({"SameParameterValue"})
    static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> accessibilityService) {
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
    public static synchronized boolean runSuJavaWithAppProcess(Context context, Class<?> cls, @NonNull String... args) {
        final long start = SystemClock.elapsedRealtime();
        final String packageResourcePath = context.getPackageResourcePath();
        final String className = cls.getName();
        final String argLine = TextUtils.join(" ", args);
//        final String cmdDir = "/system/bin";
        final String cmdDir = File.separator;
        Process suProcess = null;
        int exitCode = Integer.MIN_VALUE;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream stream = new DataOutputStream(suProcess.getOutputStream());
            stream.writeBytes("export CLASSPATH=" + packageResourcePath + '\n');
            stream.writeBytes("exec app_process " + cmdDir + " " + className + " " + argLine + '\n');
            stream.flush();
        } catch (IOException e) {
            DebugLog.e(TAG, "runSuJavaWithAppProcess: " +
                    "blocked in " + (SystemClock.elapsedRealtime() - start) + " ms.", e);
            return false;
        } finally {
            if (suProcess != null) {
                try {
                    exitCode = suProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        DebugLog.i(TAG, "runSuJavaWithAppProcess: exit code = " + exitCode +
                ", blocked in " + (SystemClock.elapsedRealtime() - start) + " ms.");
        return exitCode == 0;
    }

    static void setComponentEnabled(Context context, Class<?> componentClass, boolean enabled) {
        if (getComponentEnabled(context, componentClass) != enabled) {
            DebugLog.d(TAG, "setComponentEnabled: Update state: " + Arrays.asList(componentClass.getSimpleName(), enabled));
            PackageManager pm = context.getPackageManager();
            ComponentName cn = new ComponentName(context, componentClass);
            pm.setComponentEnabledSetting(cn,
                    enabled ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    static boolean getComponentEnabled(Context context, Class<?> componentClass) {
        final int state = context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, componentClass));
        return state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
    }

    static SparseArray<String> getClassIntApiConstant(Class<?> clz, String prefix) {
        SparseArray<String> container = new SparseArray<>();
        for (Field field : clz.getFields()) {
            String name = field.getName();
            if (name.startsWith(prefix)) {
                try {
                    container.put(field.getInt(null), name);
                } catch (IllegalAccessException e) {
                    DebugLog.w(TAG, "getClassIntConstant: name = " + name, e);
                }
            }
        }
        return container;
    }
}
