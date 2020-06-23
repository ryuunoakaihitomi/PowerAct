package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("deprecation")
public final class PaFragment extends Fragment {

    private static final String TAG = "PaFragment";

    // For DevicePolicyManager
    private int mRequestCode;

    private boolean mHasRequestedAccessibility, mIsShowPowerDialog, mFirstRun;

    private Callback mCallback = new Callback() {

        @Override
        public void done() {
            DebugLog.d(TAG, "done: normal callback.");
        }

        @Override
        public void failed() {
            DebugLog.d(TAG, "failed: normal callback.");
        }
    };

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminReceiverComponentName;
    private Activity mAssociatedActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initialize();
    }

    private void initialize() {
        mAssociatedActivity = getActivity();
        if (mDevicePolicyManager == null) {
            mDevicePolicyManager = (DevicePolicyManager) mAssociatedActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if (mAdminReceiverComponentName == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                Utils.setComponentEnabled(mAssociatedActivity, PaReceiver.class, true);
            }
            mAdminReceiverComponentName = new ComponentName(mAssociatedActivity, PaReceiver.class);
        }
    }

    void requestAction(Callback callback, boolean isShowPowerDialog) {
        mIsShowPowerDialog = isShowPowerDialog;
        Activity activity = getActivity();
        if (callback != null) mCallback = callback;
        if (activity == null) {
            DebugLog.e(TAG, "requestAction: Activity does not prepare!");
            failed("getActivity() is null.");
            return;
        }
        initialize();
        boolean isAdminActive = mDevicePolicyManager.isAdminActive(mAdminReceiverComponentName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || mIsShowPowerDialog) {
            if (isAdminActive && !mIsShowPowerDialog) {
                // lockScreen & adminActive, remove admin automatically.
                mDevicePolicyManager.removeActiveAdmin(mAdminReceiverComponentName);
                Utils.setComponentEnabled(activity, PaReceiver.class, false);
            }
            if (!Utils.isAccessibilityServiceEnabled(activity, PaService.class)) {
                if (!Utils.getComponentEnabled(activity, PaService.class)) {
                    Utils.setComponentEnabled(activity, PaService.class, true);
                    // Not be effective immediately. Wait for next request...
                    failed("AccessibilityService disabled.");
                    return;
                }
                try {
                    // If you disabled PaService, and try to enable it.
                    // You cannot find it in Accessibility Settings instantly.
                    DebugLog.d(TAG, "requestAction: Try to enable Accessibility Service...");
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
        } else {
            if (isAdminActive) {
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

    private void requireAccessibilityAction() {
        // Send broadcast to show power dialog (21+) or to lock screen (28+).
        boolean receiverState =
                PaService.sendAction(mAssociatedActivity,
                        mIsShowPowerDialog ? PaService.POWER_DIALOG_ACTION : PaService.LOCK_SCREEN_ACTION);
        if (receiverState) {
            done();
        } else {
            failed("Unregistered BroadcastReceiver.");
        }
    }

    private void done() {
        DebugLog.d(TAG, "done...");
        mCallback.done();
        detach();
    }

    private void failed(String reason) {
        DebugLog.i(TAG, "failed... Reason: " + reason);
        mCallback.failed();
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
}
