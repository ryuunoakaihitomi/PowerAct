package github.ryuunoakaihitomi.poweract;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

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
        Log.d(TAG, "isAccessibilityServiceEnabled: Enabled services are " + enabledServicesSetting);
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

    public static synchronized boolean runSuJavaWithAppProcess(Context context, Class<?> cls, @NonNull String... args) {
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
            Log.e(TAG, "runSuJavaWithAppProcess: ", e);
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
        Log.i(TAG, "runSuJavaWithAppProcess: exit code is " + exitCode);
        return exitCode == 0;
    }
}
