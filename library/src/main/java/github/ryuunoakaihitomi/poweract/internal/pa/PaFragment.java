package github.ryuunoakaihitomi.poweract.internal.pa;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.util.Arrays;
import java.util.List;

import github.ryuunoakaihitomi.poweract.BuildConfig;
import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.LibraryCompat;
import github.ryuunoakaihitomi.poweract.internal.util.ShizukuCompat;
import github.ryuunoakaihitomi.poweract.internal.util.SystemCompat;
import github.ryuunoakaihitomi.poweract.internal.util.UserGuideRunnable;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;
import github.ryuunoakaihitomi.poweract.internal.util.VerboseTimingLogger;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.ShizukuProvider;
import rikka.shizuku.SystemServiceHelper;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("deprecation")
public final class PaFragment extends Fragment implements Shizuku.OnRequestPermissionResultListener {

    private static final String TAG = "PaFragment";

    // For DevicePolicyManager / Shizuku (>=0)
    private int mRequestCode;

    @PaConstants.ActionType
    private int mAction;

    // Check the status returned from the Accessibility settings.
    private boolean mHasRequestedAccessibility, mFirstRun;

    private Callback mCallback;

    private DevicePolicyManager mDpm;
    private ComponentName mAdminReceiverComponentName;
    private Activity mAssociatedActivity;

    /**
     * Measure the time user spends on the "next steps".
     * There're some custom environments that seem to delay user from doing it on purpose.
     */
    private final VerboseTimingLogger mUserDelayLogger = new VerboseTimingLogger(DebugLog.formatTag(TAG), "user delay");

    @Override
    public void onAttach(Context context) {
        DebugLog.v(TAG, "onAttach @" + Integer.toHexString(System.identityHashCode(this)));
        super.onAttach(context);
        initialize();
    }

    private void initialize() {
        mAssociatedActivity = getActivity();
        if (mDpm == null) {
            mDpm = (DevicePolicyManager) mAssociatedActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if (mAdminReceiverComponentName == null) {
            Utils.setComponentEnabled(mAssociatedActivity, PaReceiver.class, true);
            mAdminReceiverComponentName = new ComponentName(mAssociatedActivity, PaReceiver.class);
        }
    }

    public void requestAction(Callback callback, @PaConstants.ActionType int action) {
        mAction = action;
        Activity activity = getActivity();
        mCallback = callback;
        if (activity == null) {
            DebugLog.e(TAG, "requestAction: Activity does not prepare!");
            failed("getActivity() is null.");
            return;
        }
        initialize();

        if ((
                // Using Shizuku to lock screen instead of DPM before 28 to avoid "secure unlock" and "complex uninstalling".
                (mAction == PaConstants.ACTION_LOCK_SCREEN && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) ||
                        // Using Shizuku to reboot instead of DPM since R to avoid registering device owner.
                        // REBOOT permission granted to Shell since 30. commit id: bf19417b0dc3747bfd8c4cf84817ac98d382a665
                        (mAction == PaConstants.ACTION_REBOOT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        ) && LibraryCompat.isShizukuPrepared()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ShizukuCompat.checkSelfPermission(activity) != PackageManager.PERMISSION_GRANTED) {
                    // The users of Shizuku Manager can be treated as advanced users. So it's unnecessary to guide them.
                    //UserGuideRunnable.run();
                    mRequestCode = Utils.randomNaturalNumber();
                    if (!Shizuku.isPreV11()) Shizuku.addRequestPermissionResultListener(this);
                    ShizukuCompat.requestPermission(this, mRequestCode);
                    mUserDelayLogger.addSplit("shizuku permission");
                } else {
                    callShizuku();
                }
            }
            UserGuideRunnable.release();
            return;
        }

        boolean isAdminActive = mDpm.isAdminActive(mAdminReceiverComponentName);
        boolean isDeviceOwner = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            isDeviceOwner = mDpm.isDeviceOwnerApp(activity.getPackageName());
        }
        /* The action AccessibilityService must be used.
           Show power dialog and lock screen by it. (>=28) */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mAction != PaConstants.ACTION_REBOOT || mAction == PaConstants.ACTION_POWER_DIALOG) {
            // Attempt to launch accessibility settings, the services in work profile are not shown. So we have no way to enable it.
            if (Utils.isInWorkProfile(activity)) {
                failed("Cannot enable accessibility service in work profile!");
                return;
            }
            // The device admin is no longer useful.
            if (isAdminActive && mAction == PaConstants.ACTION_LOCK_SCREEN) {
                // lockScreen & adminActive, remove admin automatically.
                mDpm.removeActiveAdmin(mAdminReceiverComponentName);
            }
            if (!Utils.isAccessibilityServiceEnabled(activity, PaService.class)) {
                if (!Utils.getComponentEnabled(activity, PaService.class)) {
                    Utils.setComponentEnabled(activity, PaService.class, true);
                    // Not be effective immediately. Wait for next request...
                    failed("AccessibilityService disabled.");
                    return;
                }
                // On Android 11, force stop app may prevent accessibility service from being enabled next time.
                // Warn developer about that.
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                    List<ApplicationExitInfo> historicalProcessExitReasons = activity.getSystemService(ActivityManager.class)
                            .getHistoricalProcessExitReasons(activity.getPackageName(), 0, 1);
                    if (historicalProcessExitReasons.size() > 0) {
                        ApplicationExitInfo appExitInfo = historicalProcessExitReasons.get(0);
                        if (appExitInfo.getReason() == ApplicationExitInfo.REASON_USER_REQUESTED) {
                            DebugLog.i(TAG, "requestAction: " + appExitInfo);
                            DebugLog.w(TAG, "requestAction: On Android 11, force stop app may prevent accessibility service from being enabled next time.");
                        }
                    }
                }

                // If you disabled PaService, and try to enable it.
                // You cannot find it in Accessibility Settings instantly.
                DebugLog.d(TAG, "requestAction: Try to enable Accessibility Service...");
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivitySafely(intent, false, () -> {
                    UserGuideRunnable.run();
                    mHasRequestedAccessibility = true;
                    mUserDelayLogger.addSplit("accessibility service");
                });
            } else {
                requireAccessibilityAction();
            }
        }
        /* The action DevicePolicyManager be used.
           reboot and lock screen by it. (<28) */
        else {
            if (action == PaConstants.ACTION_REBOOT) {
                /*
                 * Prevent monkey from performing some of the functions to interrupt test or make the other blunders.
                 * In fact, such a protection mechanism has been implemented within the system.
                 * {ADB, OemUnlock, bug report, AddUser, AutoSyncData, [MasterClearConfirm(Car), ResetConfirm(Tv)],
                 * resetNetwork(Car), showDeveloperOptions(Car), Storage, FlashlightTile, RemoveAccount(Tv)}
                 */
                if (ActivityManager.isUserAMonkey()) {
                    failed("monkey");
                    return;
                }
                if (isDeviceOwner) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            mDpm.reboot(mAdminReceiverComponentName);
                            done();
                        }
                        // No active admin ComponentInfo{..}
                        catch (SecurityException e) {
                            failed(e.getMessage());
                        } catch (IllegalStateException e) {
                            failed("Outgoing call?");
                        }
                    }
                } else {
                    failed("Not a device owner.");
                }
            }
            // lock screen by dpm.
            else if (isAdminActive) {
                mDpm.lockNow();
                done();
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminReceiverComponentName);
                startActivitySafely(intent, true, () -> mUserDelayLogger.addSplit("device admin"));
            }
        }
    }

    private void startActivitySafely(@NonNull Intent intent, boolean hasResult, Runnable onSuccessCallback) {
        try {
            if (hasResult) {
                mRequestCode = Utils.randomNaturalNumber();
                startActivityForResult(intent, mRequestCode);
            } else {
                startActivity(intent);
            }
            onSuccessCallback.run();
        } catch (Throwable t) {
            // ANE is a expected exception. JavaDoc of Settings.ACTION_ACCESSIBILITY_SETTINGS:
            // "In some cases, a matching Activity may not exist, so ensure you safeguard against this."
            if (t instanceof ActivityNotFoundException) {
                failed(intent.getAction() + " not found!");
            } else {
                DebugLog.e(TAG, "startActivitySafely: WEIRD. But we have avoided it.", t);
                failed(t.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DebugLog.d(TAG, "onResume");
        if (!mFirstRun) {
            DebugLog.d(TAG, "onResume: First run. There's not accessibility settings ui now.");
            mFirstRun = true;
        } else if (mHasRequestedAccessibility) {
            mHasRequestedAccessibility = false;
            mUserDelayLogger.addSplit("return from accessibility service");
            if (Utils.isAccessibilityServiceEnabled(mAssociatedActivity, PaService.class)) {
                requireAccessibilityAction();
            } else {
                failed("Accessibility Service is still disabled.");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DebugLog.d(TAG, "onActivityResult");
        if (mRequestCode == requestCode) {
            mUserDelayLogger.addSplit("return from device admin");
            if (resultCode == Activity.RESULT_OK) {
                if (mDpm.isAdminActive(mAdminReceiverComponentName)) {
                    mDpm.lockNow();
                    done();
                }
            } else {
                // resultCode != Activity.RESULT_OK
                failed("resultCode(" + resultCode + ") != Activity.RESULT_OK(-1)");
            }
        } else {
            // mRequestCode != requestCode
            failed("mRequestCode != requestCode " + Arrays.asList(mRequestCode, requestCode));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length == 0 || grantResults.length == 0) {
            // SHOULD NEVER HAPPEN! ( M I U I )
            failed("Empty permissions / grantResults.");
            return;
        }
        if (ShizukuProvider.PERMISSION.equals(permissions[0])) {
            onRequestPermissionResult(requestCode, grantResults[0]);
        } else {
            failed("Unknown permission: " + Arrays.asList(permissions));
        }
    }

    /**
     * @see Shizuku.OnRequestPermissionResultListener
     * @see ShizukuProvider#PERMISSION
     */
    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == mRequestCode &&
                grantResult == PackageManager.PERMISSION_GRANTED) {
            mUserDelayLogger.addSplit("return from shizuku permission (granted)");
            callShizuku();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ShizukuCompat.shouldShowRequestPermissionRationale(this)) {
                DebugLog.e(TAG, "onRequestPermissionsResult: Shizuku permission denied forever!");
            }
            failed("requestCode(REQUEST_CODE_SHIZUKU_PERMISSION=" + mRequestCode + "),grantResults -> " +
                    Arrays.asList(requestCode, grantResult));
        }
    }

    //@RequiresPermission(ShizukuApiConstants.PERMISSION)
    private void callShizuku() {
        try {
            SystemCompat.setPowerBinder(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.POWER_SERVICE)));
            switch (mAction) {
                case PaConstants.ACTION_LOCK_SCREEN:
                    SystemCompat.goToSleep();
                    break;
                case PaConstants.ACTION_REBOOT:
                    SystemCompat.reboot(null);
                    break;
                case PaConstants.ACTION_POWER_DIALOG: // Only for keeping "switch" statement. @IntDef
            }
            // Update DPM state in time.
            mDpm.removeActiveAdmin(mAdminReceiverComponentName);
            done();
        } catch (Throwable t) {
            DebugLog.e(TAG, "callShizuku", t);
            failed("Shizuku: " + t.getMessage());
        }
    }

    private void requireAccessibilityAction() {
        UserGuideRunnable.release();
        // Send broadcast to show power dialog (21+) or to lock screen (28+).
        boolean receiverState = PaService.sendAction(mAssociatedActivity,
                mAction == PaConstants.ACTION_POWER_DIALOG ? PaService.POWER_DIALOG_ACTION : PaService.LOCK_SCREEN_ACTION,
                mCallback);
        if (!receiverState) {
            failed("Unregistered BroadcastReceiver.");
        }
    }

    private void done() {
        DebugLog.d(TAG, "done...");
        CallbackHelper.of(mCallback).done();
        detach();
    }

    private void failed(String reason) {
        DebugLog.i(TAG, "failed... Action:" +
                Utils.getClassIntApiConstantString(PaConstants.class, "ACTION", mAction) +
                " Reason:" + reason);
        CallbackHelper.of(mCallback).failed();
        detach();
    }

    private void detach() {
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        DebugLog.v(TAG, "onDetach");
        if (LibraryCompat.isShizukuPrepared() && !Shizuku.isPreV11())
            Shizuku.removeRequestPermissionResultListener(this);

        /* Logging user delay */
        mUserDelayLogger.addSplit("detach");
        mUserDelayLogger.setDisabled(!BuildConfig.DEBUG);
        mUserDelayLogger.dumpToLog();

        // Enable "Don't keep activities" in Developer options to debug this case.
        if (isAdded()) {
            DebugLog.w(TAG, "onDetach: The associated activity was recycled by system. " +
                    "Some unexpected behaviours may trigger!");
            /* Caused by low memory? */
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) mAssociatedActivity.getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(memoryInfo);
            DebugLog.i(TAG, "onDetach: mem info: [" + memoryInfo.availMem +
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? "/" + memoryInfo.totalMem : "") +
                    "], threshold=" + memoryInfo.threshold +
                    ", lowMemory=" + memoryInfo.lowMemory +
                    ", isLowMemoryDevice=" + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && am.isLowRamDevice()) +
                    ", isBackgroundRestricted=" + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && am.isBackgroundRestricted()));
        }
    }

    /**
     * Developer Note:
     * <p>
     * Don't {@link #detach()} it in this method,
     * {@link #mCallback} will be null.
     *
     * @param level {@link ComponentCallbacks2}
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (BuildConfig.DEBUG)
            DebugLog.d(TAG, "onTrimMemory: " + Utils.getClassIntApiConstantString(ComponentCallbacks2.class, "TRIM", level));
    }
}
