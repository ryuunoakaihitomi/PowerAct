package github.ryuunoakaihitomi.poweract;

import android.accessibilityservice.AccessibilityService;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import java.util.Arrays;

import github.ryuunoakaihitomi.poweract.internal.Initializer;
import github.ryuunoakaihitomi.poweract.internal.pa.PaReceiver;
import github.ryuunoakaihitomi.poweract.internal.pa.PaService;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.LibraryCompat;
import github.ryuunoakaihitomi.poweract.internal.util.UserGuideRunnable;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;

/**
 * Public utility class of PowerAct library.
 *
 * @since 1.0.8
 */
public final class ExternalUtils {

    private static final String TAG = "ExternalUtils";

    private ExternalUtils() {
    }

    static {
        Initializer.notify(TAG);
    }

    /**
     * {@link PowerAct} depends on {@link AccessibilityService}
     * or/and {@link DeviceAdminReceiver} for providing permissions to perform actions.
     * The components must be registered in <code>AndroidManifest.xml</code>.
     * So they are visible in <code>Settings</code> even before use.
     * <p>
     * The library will enable or disable them automatically in different cases,
     * but we can disable them manually as soon as possible in order not to confuse the user.
     *
     * @param context Context for building {@link ComponentName}
     *                and {@link BroadcastReceiver}.
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String myPkgName = context.getPackageName();
                    if (dpm.isDeviceOwnerApp(myPkgName)) {
                        dpm.clearDeviceOwnerApp(myPkgName);
                    }
                }
                try {
                    // The operation is not supported on Wear.
                    dpm.removeActiveAdmin(new ComponentName(context, PaReceiver.class));
                } catch (UnsupportedOperationException e) {
                    DebugLog.e(TAG, "disableExposedComponents: [removeActiveAdmin] " +
                            e.getMessage());
                }
            }
            Utils.setComponentEnabled(context, PaReceiver.class, false);
        }
        if (serviceEnabled) {
            PaService.sendAction(context, PaService.DISABLE_SERVICE_ACTION, null);
            Utils.setComponentEnabled(context, PaService.class, false);
        }
    }

    /**
     * Enable or disable logcat output.
     * Logcat output is enabled by default for debugging. We can filter the logcat by tags that contain "<b>PowerAct_</b>".
     * <p>
     * Example: We can disable log output in the release version:
     * <pre>
     * ExternalUtils.enableLog(BuildConfig.DEBUG);
     * </pre>
     *
     * @param isEnabled ↑
     * @since 1.0.13
     */
    public static void enableLog(boolean isEnabled) {
        DebugLog.enabled = isEnabled;
        /* Enabling other dependencies' logcat */
        if (LibraryCompat.isLibsuAvailable()) {
            Shell.enableVerboseLogging = isEnabled;
        }
    }

    /**
     * Sometimes we need to guide user to grant permission the library needs manually,
     * such as {@link AccessibilityService} or root permission.
     * Especially {@link AccessibilityService},
     * some custom systems deliberately put the associated option into a invisible place
     * (for example, beyond the bottom of the visible range of the screen).
     * <p>
     * If the library thinks that we should guide user, the {@link Runnable} argument will be executed.
     * <p>
     * After setting up the {@link Runnable} is done, <b>we must use the library AS SOON AS POSSIBLE!</b>
     * It will be null after 5 seconds in order to recycle memory.
     * <p>
     * It doesn't work in Shizuku permission request. We can treat users using Shizuku Manager as advanced users, so we don't have to guide them.
     *
     * @param runnable How do you want to guide the user?
     * @see UserGuideRunnable#RELEASE_DELAY_TIME_MILLIS
     * @since 1.1.0
     */
    public static void setUserGuideRunnable(@NonNull Runnable runnable) {
        UserGuideRunnable.set(runnable);
    }

    /**
     * Whether the exposed component(s) is/are available or not.
     *
     * @param context {@link Utils#getComponentEnabled(Context, Class)}
     * @return Should be false after calling {{@link #disableExposedComponents(Context)}}
     * @see PaService
     * @see PaReceiver
     * @since 1.2.2
     */
    public static boolean isExposedComponentAvailable(@NonNull Context context) {
        final boolean
                receiverEnabled = Utils.getComponentEnabled(context, PaReceiver.class),
                serviceEnabled = Utils.getComponentEnabled(context, PaService.class),
                result;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            result = receiverEnabled;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            result = receiverEnabled && serviceEnabled;
        } else {
            result = serviceEnabled;
        }
        DebugLog.d(TAG, "isExposedComponentsAvailable: " +
                "ret = " + result + ", rcv = " + receiverEnabled + ", srv = " + serviceEnabled);
        return result;
    }
}
