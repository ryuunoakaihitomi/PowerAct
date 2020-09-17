package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;

import github.ryuunoakaihitomi.poweract.internal.Initializer;
import github.ryuunoakaihitomi.poweract.internal.pa.PaConstants;
import github.ryuunoakaihitomi.poweract.internal.pa.PaFragment;
import github.ryuunoakaihitomi.poweract.internal.pa.PaReceiver;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;

/**
 * Provide the main functions of the library.
 * <p>
 * Hello! I am <b>PowerAct</b>.
 *
 * @since 1.0.0
 */
@MainThread
@SuppressWarnings("WeakerAccess")
public class PowerAct {

    private static final String TAG = "PowerAct";

    private PowerAct() {
    }

    static {
        Initializer.notify(TAG);
    }

    /**
     * Go to see {@link #lockScreen(Activity, Callback)}.
     *
     * @param activity As {@link #lockScreen(Activity, Callback)}'s <code>activity</code>.
     */
    public static void lockScreen(@NonNull Activity activity) {
        lockScreen(activity, null);
    }

    /**
     * Make the device lock immediately, as if the lock screen timeout has expired at the point of this call.
     * <p>
     * Use {@link android.app.admin.DevicePolicyManager} to lock before 28, Since 28 use
     * {@link android.accessibilityservice.AccessibilityService} to lock in order to
     * unlock by biometric sensors.
     *
     * @param activity Be used to open specific permission request UI and perform power operations.
     *                 Should not be null.
     * @param callback To return operation status.
     *                 Can be null.
     * @see android.app.admin.DevicePolicyManager#lockNow()
     * @see android.accessibilityservice.AccessibilityService#GLOBAL_ACTION_LOCK_SCREEN
     */
    public static void lockScreen(@NonNull Activity activity, @Nullable Callback callback) {
        requestAction(activity, callback, PaConstants.ACTION_LOCK_SCREEN);
    }

    /**
     * Go to see {@link #showPowerDialog(Activity, Callback)}.
     *
     * @param activity As {@link #lockScreen(Activity, Callback)}'s <code>activity</code>.
     */
    public static void showPowerDialog(@NonNull Activity activity) {
        showPowerDialog(activity, null);
    }

    /**
     * To open the power long-press dialog.
     * <b>The operation can only be available since 21.</b>
     *
     * @param activity As {@link #lockScreen(Activity, Callback)}'s <code>activity</code>.
     * @param callback As {@link #lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @see android.accessibilityservice.AccessibilityService#GLOBAL_ACTION_POWER_DIALOG
     */
    public static void showPowerDialog(@NonNull Activity activity, @Nullable Callback callback) {
        requestAction(activity, callback, PaConstants.ACTION_POWER_DIALOG);
    }

    /**
     * Go to see {@link #reboot(Activity, Callback)}.
     *
     * @param activity As {@link #lockScreen(Activity, Callback)}'s <code>activity</code>.
     * @since 1.0.18
     */
    public static void reboot(@NonNull Activity activity) {
        reboot(activity, null);
    }

    /**
     * Reboot the device. Be available since 24.
     * <p>
     * <b>Using it is VERY DIFFICULT for general user.</b>
     * We have to guide user to enable the {@link PaReceiver}(may be disabled by {@link ExternalUtils#disableExposedComponents(Context)})
     * and the <i>device owner</i> for it.
     * <p>
     * Note that it will bring other restrictions.
     * In some custom environments, <i>device owner</i> may not be available or bring some compatibility issues.
     * <p>
     * <b>BE CAREFUL TO USE IT!</b>
     * Try to use {@link #showPowerDialog(Activity, Callback)} or {@link PowerActX#reboot(Callback)} instead of it if possible.
     *
     * @param activity As {@link #lockScreen(Activity, Callback)}'s <code>activity</code>.
     * @param callback As {@link #lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @see DevicePolicyManager#reboot(ComponentName)
     * @see <a href="https://source.android.com/devices/tech/admin/testing-setup#set_up_the_device_owner_for_testing">Set up device owner for testing</a>
     * @see <a href="https://developer.android.com/work/dpc/device-management#remotely_reboot_an_android_device">Remotely reboot an Android device</a>
     * @see <a href="https://stackoverflow.com/questions/21183328/how-to-make-my-app-a-device-owner">How to make my app a device owner?</a>
     * @since 1.0.18
     */
    public static void reboot(@NonNull Activity activity, @Nullable Callback callback) {
        requestAction(activity, callback, PaConstants.ACTION_REBOOT);
    }

    private static void requestAction(final Activity activity, final Callback callback, final @PaConstants.ActionType int action) {
        if (!Utils.isMainThread()) {
            DebugLog.e(TAG, "requestAction: Must be called from main thread!");
            CallbackHelper.of(callback).failed();
            return;
        }
        if (activity == null) {
            DebugLog.e(TAG, "requestAction: Activity is null!");
            CallbackHelper.of(callback).failed();
            return;
        }
        if (action == PaConstants.ACTION_POWER_DIALOG && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            DebugLog.e(TAG, "requestAction: Cannot show power dialog before API level 21!");
            CallbackHelper.of(callback).failed();
            return;
        }
        if (action == PaConstants.ACTION_REBOOT && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            DebugLog.e(TAG, "requestAction: Cannot reboot before API level 24!");
            CallbackHelper.of(callback).failed();
            return;
        }
        final FragmentManager manager = activity.getFragmentManager();
        final Fragment sourceFragment = manager.findFragmentByTag(TAG);
        final PaFragment fragment;
        try {
            fragment = (PaFragment) sourceFragment;
        } catch (ClassCastException e) {
            DebugLog.e(TAG, "requestAction: Occupied \"PowerAct\" fragment: " +
                    sourceFragment + "   Dump it to System.out...");
            if (DebugLog.enabled) {
                sourceFragment.dump("", null, new PrintWriter(System.out, true), null);
            }
            CallbackHelper.of(callback).failed();
            return;
        }
        if (fragment == null) {
            DebugLog.i(TAG, "requestAction: Initializing fragment...");
            final Fragment invisibleFragment = new PaFragment();
            final FragmentTransaction transaction = manager.beginTransaction().add(invisibleFragment, TAG);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                transaction.commitNow();
            } else {
                transaction.commit();
                manager.executePendingTransactions();
            }
            requestAction(activity, callback, action);
        } else {
            fragment.requestAction(callback, action);
        }
    }
}
