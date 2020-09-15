package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
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
import java.util.Random;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuBinderWrapper;
import moe.shizuku.api.SystemServiceHelper;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("deprecation")
public final class PaFragment extends Fragment {

    private static final String TAG = "PaFragment";

    private static final int REQUEST_CODE_SHIZUKU_PERMISSION = 1;

    // For DevicePolicyManager
    private int mRequestCode;

    @PowerAct.ActionType
    private int mAction;

    // Check the status returned from the Accessibility settings.
    private boolean mHasRequestedAccessibility, mFirstRun;

    private Callback mCallback;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminReceiverComponentName;
    private Activity mAssociatedActivity;

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

    void requestAction(Callback callback, @PowerAct.ActionType int action) {
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
        if (mAction == PowerAct.ACTION_LOCK_SCREEN &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P &&
                LibraryCompat.isShizukuPrepared(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.checkSelfPermission(ShizukuApiConstants.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                    // The users of Shizuku Manager can be treated as advanced users. So it's unnecessary to guide them.
                    //UserGuideRunnable.run();
                    requestPermissions(new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_SHIZUKU_PERMISSION);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mAction != PowerAct.ACTION_REBOOT || mAction == PowerAct.ACTION_POWER_DIALOG) {
            // The device admin is no longer useful.
            if (isAdminActive && mAction == PowerAct.ACTION_LOCK_SCREEN) {
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
            if (action == PowerAct.ACTION_REBOOT) {
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
                mRequestCode = Math.abs(new Random().nextInt());
                try {
                    startActivityForResult(intent, mRequestCode);
                } catch (ActivityNotFoundException e) {
                    // ActivityNotFoundException: DevicePolicyManager.EXTRA_DEVICE_ADMIN
                    failed(e.getMessage());
                } catch (AndroidRuntimeException e) {
                    // Special env: Xiaomi DuoKan E-reader (Allwinner EPD106)
                    // Unknown error code -1 when starting Intent { act=android.app.action.ADD_DEVICE_ADMIN (has extras) }
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
            if (Utils.isAccessibilityServiceEnabled(mAssociatedActivity, PaService.class)) {
                requireAccessibilityAction();
            } else {
                failed("Accessibility Service is still disabled.");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebugLog.d(TAG, "onActivityResult");
        if (mRequestCode == requestCode) {
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
        if (requestCode == REQUEST_CODE_SHIZUKU_PERMISSION &&
                ShizukuApiConstants.PERMISSION.equals(permissions[0]) &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lockScreenByShizuku();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale(ShizukuApiConstants.PERMISSION)) {
                DebugLog.e(TAG, "onRequestPermissionsResult: Shizuku permission denied forever!");
            }
            failed("requestCode(REQUEST_CODE_SHIZUKU_PERMISSION=" + REQUEST_CODE_SHIZUKU_PERMISSION + "),permissions,grantResults -> " +
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
                mAction == PowerAct.ACTION_POWER_DIALOG ? PaService.POWER_DIALOG_ACTION : PaService.LOCK_SCREEN_ACTION,
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
                Utils.getClassIntApiConstantString(PowerAct.class, "ACTION", mAction) +
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
