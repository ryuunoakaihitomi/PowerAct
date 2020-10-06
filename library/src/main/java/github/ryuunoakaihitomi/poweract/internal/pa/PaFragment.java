package github.ryuunoakaihitomi.poweract.internal.pa;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.util.AndroidRuntimeException;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.util.Arrays;

import github.ryuunoakaihitomi.poweract.BuildConfig;
import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.LibraryCompat;
import github.ryuunoakaihitomi.poweract.internal.util.SystemCompat;
import github.ryuunoakaihitomi.poweract.internal.util.UserGuideRunnable;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;
import github.ryuunoakaihitomi.poweract.internal.util.VerboseTimingLogger;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuBinderWrapper;
import moe.shizuku.api.SystemServiceHelper;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("deprecation")
public final class PaFragment extends Fragment {

    private static final String TAG = "PaFragment";

    // For DevicePolicyManager / Shizuku (>=0)
    private int mRequestCode;

    @PaConstants.ActionType
    private int mAction;

    // Check the status returned from the Accessibility settings.
    private boolean mHasRequestedAccessibility, mFirstRun;

    private Callback mCallback;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminReceiverComponentName;
    private Activity mAssociatedActivity;

    /**
     * Measure the time user spends on the "next steps".
     * There're some custom environments that seem to delay user from doing it on purpose.
     */
    private VerboseTimingLogger mUserDelayLogger = new VerboseTimingLogger(DebugLog.formatTag(TAG), "user delay");

    @Override
    public void onAttach(Context context) {
        DebugLog.v(TAG, "onAttach");
        super.onAttach(context);
        initialize();
    }

    private void initialize() {
        mAssociatedActivity = getActivity();
        if (mDevicePolicyManager == null) {
            mDevicePolicyManager = (DevicePolicyManager) mAssociatedActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
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

        /* Use Shizuku instead of DPM before 28 to avoid "secure unlock" and "complex uninstalling". */
        if (mAction == PaConstants.ACTION_LOCK_SCREEN &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P &&
                LibraryCompat.isShizukuPrepared(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.checkSelfPermission(ShizukuApiConstants.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                    // The users of Shizuku Manager can be treated as advanced users. So it's unnecessary to guide them.
                    //UserGuideRunnable.run();
                    mRequestCode = Utils.randomNaturalNumber();
                    requestPermissions(new String[]{ShizukuApiConstants.PERMISSION}, mRequestCode);
                    mUserDelayLogger.addSplit("shizuku permission");
                } else {
                    lockScreenByShizuku();
                }
            }
            UserGuideRunnable.release();
            return;
        }

        boolean isAdminActive = mDevicePolicyManager.isAdminActive(mAdminReceiverComponentName);
        boolean isDeviceOwner = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(activity.getPackageName());
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
                mDevicePolicyManager.removeActiveAdmin(mAdminReceiverComponentName);
            }
            if (!Utils.isAccessibilityServiceEnabled(activity, PaService.class)) {
                if (!Utils.getComponentEnabled(activity, PaService.class)) {
                    Utils.setComponentEnabled(activity, PaService.class, true);
                    // Not be effective immediately. Wait for next request...
                    failed("AccessibilityService disabled.");
                    return;
                }
                /*
                 * It's just a waste of time implementing some additional demands with these APIs.
                 *
                 * @see DevicePolicyManager#setSecureSetting(ComponentName, String, String)
                 * @see DevicePolicyManager#setPermittedAccessibilityServices(ComponentName, List)
                 * @see DevicePolicyManager#setUninstallBlocked(ComponentName, String, boolean)
                 */
                try {
                    // If you disabled PaService, and try to enable it.
                    // You cannot find it in Accessibility Settings instantly.
                    DebugLog.d(TAG, "requestAction: Try to enable Accessibility Service...");
                    UserGuideRunnable.run();
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    mHasRequestedAccessibility = true;
                    mUserDelayLogger.addSplit("accessibility service");
                } catch (ActivityNotFoundException e) {
                    // ActivityNotFoundException: Settings.ACTION_ACCESSIBILITY_SETTINGS
                    failed(e.getMessage());
                } /* For some weird env. */ catch (SecurityException e) {
                    DebugLog.e(TAG, "requestAction: ", e);
                    failed("SecurityException");
                }
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
                            mDevicePolicyManager.reboot(mAdminReceiverComponentName);
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
                mDevicePolicyManager.lockNow();
                done();
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminReceiverComponentName);
                mRequestCode = Utils.randomNaturalNumber();
                try {
                    startActivityForResult(intent, mRequestCode);
                    mUserDelayLogger.addSplit("device admin");
                } catch (ActivityNotFoundException e) {
                    // ActivityNotFoundException: DevicePolicyManager.EXTRA_DEVICE_ADMIN
                    failed(e.getMessage());
                } catch (AndroidRuntimeException e) {
                    // Special env: Xiaomi DuoKan E-reader (Allwinner EPD106)
                    // Unknown error code -1 when starting Intent { act=android.app.action.ADD_DEVICE_ADMIN (has extras) }
                    DebugLog.w(TAG, e);
                    failed(e.getMessage());
                }
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
                if (mDevicePolicyManager.isAdminActive(mAdminReceiverComponentName)) {
                    mDevicePolicyManager.lockNow();
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
        if (requestCode == mRequestCode &&
                ShizukuApiConstants.PERMISSION.equals(permissions[0]) &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mUserDelayLogger.addSplit("return from shizuku permission (granted)");
            lockScreenByShizuku();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale(ShizukuApiConstants.PERMISSION)) {
                DebugLog.e(TAG, "onRequestPermissionsResult: Shizuku permission denied forever!");
            }
            failed("requestCode(REQUEST_CODE_SHIZUKU_PERMISSION=" + mRequestCode + "),permissions,grantResults -> " +
                    Arrays.asList(requestCode, Arrays.toString(permissions), Arrays.toString(grantResults)));
        }
    }

    //@RequiresPermission(ShizukuApiConstants.PERMISSION)
    private void lockScreenByShizuku() {
        try {
            SystemCompat.setPowerBinder(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.POWER_SERVICE)));
            SystemCompat.goToSleep();
            // Update DPM state in time.
            mDevicePolicyManager.removeActiveAdmin(mAdminReceiverComponentName);
            done();
        } catch (Throwable t) {
            DebugLog.e(TAG, "lockScreenByShizuku", t);
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

        /* Logging user delay */
        mUserDelayLogger.addSplit("detach");
        mUserDelayLogger.setDisabled(!BuildConfig.DEBUG);
        mUserDelayLogger.dumpToLog();
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
