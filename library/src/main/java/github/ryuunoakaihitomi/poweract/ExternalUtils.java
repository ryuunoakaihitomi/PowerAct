package github.ryuunoakaihitomi.poweract;


import android.content.Context;
import android.util.Log;

import java.util.Arrays;

/**
 * Public utilities.
 */
public class ExternalUtils {

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
    public static void disableExposedComponents(Context context) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "disableExposedComponents: Current states [receiver, service]: " +
                    Arrays.asList(
                            Utils.getComponentEnabled(context, PaReceiver.class),
                            Utils.getComponentEnabled(context, PaService.class)));
        }
        Utils.setComponentEnabled(context, PaReceiver.class, false);
        PaService.sendAction(context, PaService.DISABLE_SERVICE_ACTION);
        Utils.setComponentEnabled(context, PaService.class, false);
    }
}
