package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.os.Build;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.PrintWriter;

import github.ryuunoakaihitomi.poweract.internal.Initializer;
import github.ryuunoakaihitomi.poweract.internal.pa.PaConstants;
import github.ryuunoakaihitomi.poweract.internal.pa.PaFragment;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;

/**
 * Hello! I am <b>PowerAct</b>.
 * <p>
 * This class provides the basic functions of this library for most mediocre environment.
 * If the environment has root privilege,
 * please consider using {@link PowerActX} in order to get more functions and reduce inherent limitations.
 *
 * @author ZQY
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
     * {@link #lockScreen(Activity, Callback)}
     *
     * @param activity a
     */
    public static void lockScreen(@NonNull Activity activity) {
        lockScreen(activity, null);
    }

    /**
     * Make the device lock immediately. Further manual operation by the user may be required.
     *
     * @param activity Be used to open specific permission request UI and perform power operations.
     * @param callback {@link Callback}
     * @see android.app.admin.DevicePolicyManager#lockNow()
     * @see android.accessibilityservice.AccessibilityService#GLOBAL_ACTION_LOCK_SCREEN
     * @see android.os.PowerManager# goToSleep()
     */
    public static void lockScreen(@NonNull Activity activity, @Nullable Callback callback) {
        requestAction(activity, callback, PaConstants.ACTION_LOCK_SCREEN);
    }

    /**
     * {@link #showPowerDialog(Activity, Callback)}
     *
     * @param activity a
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showPowerDialog(@NonNull Activity activity) {
        showPowerDialog(activity, null);
    }

    /**
     * To open the power long-press dialog. Further manual operation by the user may be required.
     *
     * @param activity {@link #lockScreen(Activity, Callback)}
     * @param callback {@link Callback}
     * @see android.accessibilityservice.AccessibilityService#GLOBAL_ACTION_POWER_DIALOG
     * @see android.view.IWindowManager# showGlobalActions()
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showPowerDialog(@NonNull Activity activity, @Nullable Callback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            DebugLog.e(TAG, "showPowerDialog: Cannot show power dialog before API level 21!");
            CallbackHelper.of(callback).failed();
            return;
        }
        requestAction(activity, callback, PaConstants.ACTION_POWER_DIALOG);
    }

    /**
     * {@link #reboot(Activity, Callback)}
     *
     * @param activity a
     * @since 1.0.18
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void reboot(@NonNull Activity activity) {
        reboot(activity, null);
    }

    /**
     * Reboot the device.
     *
     * @param activity {@link #lockScreen(Activity, Callback)}
     * @param callback {@link Callback}
     * @see android.app.admin.DevicePolicyManager#reboot(ComponentName)
     * @see android.os.PowerManager#reboot(String)
     * @since 1.0.18
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void reboot(@NonNull Activity activity, @Nullable Callback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            DebugLog.e(TAG, "reboot: Cannot reboot before API level 24!");
            CallbackHelper.of(callback).failed();
            return;
        }
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
                try {
                    transaction.commitNow();
                } catch (IllegalStateException e) {
                    DebugLog.w(TAG, "requestAction: FragmentManager is already executing transactions. Backport to commit()");
                    transaction.commit();
                    manager.executePendingTransactions();
                }
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
