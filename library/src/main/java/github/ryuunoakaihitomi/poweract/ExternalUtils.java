package github.ryuunoakaihitomi.poweract;


import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * Public utilities.
 */
public final class ExternalUtils {

    private static final String TAG = "ExternalUtils";

    private ExternalUtils() {
    }

    /**
     * {@link PowerAct} depends on {@link android.accessibilityservice.AccessibilityService}
     * or/and {@link android.app.admin.DeviceAdminReceiver} for providing permissions to perform actions.
     * The components must be registered in <code>AndroidManifest.xml</code>.
     * So they are visible in <code>Settings</code> even before use.
     * <p>
     * The library will enable or disable them automatically in different cases,
     * but we can disable them manually as soon as possible in order not to confuse the user.
     *
     * @param context Context for building {@link android.content.ComponentName}
     *                and {@link android.content.BroadcastReceiver}.
     */
    public static void disableExposedComponents(@NonNull Context context) {
        final boolean
                receiverEnabled = Utils.getComponentEnabled(context, PaReceiver.class),
                serviceEnabled = Utils.getComponentEnabled(context, PaService.class);
        if (BuildConfig.DEBUG) {
            DebugLog.i(TAG, "disableExposedComponents: Current states [receiver, service]: " +
                    Arrays.asList(receiverEnabled, serviceEnabled));
        }
        if (receiverEnabled) {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm != null) {
                dpm.removeActiveAdmin(new ComponentName(context, PaReceiver.class));
            }
            Utils.setComponentEnabled(context, PaReceiver.class, false);
        }
        if (serviceEnabled) {
            PaService.sendAction(context, PaService.DISABLE_SERVICE_ACTION);
            Utils.setComponentEnabled(context, PaService.class, false);
        }
    }

    /**
     * Enable or disable logcat output.
     * The default value is <b>true</b>.
     * <p>
     * Example: You can turn off log output in the release version:
     * <pre>
     * ExternalUtils.enableLog(BuildConfig.DEBUG);
     * </pre>
     *
     * @param isEnabled ↑
     */
    public static void enableLog(boolean isEnabled) {
        DebugLog.isOutput = isEnabled;
    }
}