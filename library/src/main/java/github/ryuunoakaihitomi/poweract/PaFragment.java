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
import android.util.Log;

import java.util.Random;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class PaFragment extends Fragment {

    private static final String TAG = "PaFragment";

    // For DevicePolicyManager
    private int mRequestCode;

    private boolean mHasRequestedAccessibility, mIsShowPowerDialog, mFirstRun;

    private Callback mCallback = new Callback() {

        @Override
        public void done() {
            Log.d(TAG, "done: normal callback.");
        }

        @Override
        public void failed() {
            Log.d(TAG, "failed: normal callback.");
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
            mAdminReceiverComponentName = new ComponentName(mAssociatedActivity, PaReceiver.class);
        }
    }

    void requestAction(Callback callback, boolean isShowPowerDialog) {
        mIsShowPowerDialog = isShowPowerDialog;
        Activity activity = getActivity();
        if (callback != null) mCallback = callback;
        if (activity == null) {
            Log.e(TAG, "requestAction: Activity does not prepare!");
            failed();
            return;
        }
        initialize();
        boolean isAdminActive = mDevicePolicyManager.isAdminActive(mAdminReceiverComponentName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || mIsShowPowerDialog) {
            if (isAdminActive && !mIsShowPowerDialog) {
                // lockScreen & adminActive, remove admin automatically.
                mDevicePolicyManager.removeActiveAdmin(mAdminReceiverComponentName);
            }
            if (!Utils.isAccessibilityServiceEnabled(activity, PaService.class)) {
                try {
                    Log.d(TAG, "requestAction: Try to enable Accessibility Service...");
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    mHasRequestedAccessibility = true;
                } catch (ActivityNotFoundException e) {
                    // ActivityNotFoundException: Settings.ACTION_ACCESSIBILITY_SETTINGS
                    failed();
                } /* For some fking weird env. */ catch (SecurityException e) {
                    Log.e(TAG, "requestAction: ", e);
                    failed();
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
                    failed();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mFirstRun) {
            Log.d(TAG, "onResume: First run. There's not accessibility settings ui now");
            mFirstRun = true;
        } else if (mHasRequestedAccessibility) {
            mHasRequestedAccessibility = false;
            if (Utils.isAccessibilityServiceEnabled(mAssociatedActivity, PaService.class)) {
                requireAccessibilityAction();
            } else {
                // Accessibility Service is still disabled.
                failed();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (mRequestCode == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                if (mDevicePolicyManager.isAdminActive(mAdminReceiverComponentName)) {
                    mDevicePolicyManager.lockNow();
                    done();
                }
            } else {
                // resultCode != Activity.RESULT_OK
                failed();
            }
        } else {
            // mRequestCode != requestCode
            failed();
        }
    }

    private void requireAccessibilityAction() {
        // Send broadcast to show power dialog (21+) or to lock screen (28+).
        Intent intent = new Intent(mIsShowPowerDialog ? PaService.POWER_DIALOG_ACTION : PaService.LOCK_SCREEN_ACTION);
        intent.setPackage(mAssociatedActivity.getPackageName());
        String token = UUID.randomUUID().toString();
        Log.d(TAG, "requireAction: Set token to " + token);
        PaService.token = token;
        intent.putExtra(PaService.EXTRA_TOKEN, token);
        mAssociatedActivity.sendBroadcast(intent);
        done();
    }

    private void done() {
        Log.d(TAG, "done...");
        mCallback.done();
        detach();
    }

    private void failed() {
        Log.d(TAG, "failed...");
        mCallback.failed();
        detach();
    }

    private void detach() {
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach");
    }
}
