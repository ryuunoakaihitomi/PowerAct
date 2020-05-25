package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.Random;

@SuppressWarnings("deprecation")
public class PaFragment extends Fragment {

    private static final String TAG = "PaFragment";
    private int mRequestCode;

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

    private void initialize() {
        Activity activity = getActivity();
        if (mDevicePolicyManager == null) {
            mDevicePolicyManager = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if (mAdminReceiverComponentName == null) {
            mAdminReceiverComponentName = new ComponentName(activity, AdminReceiver.class);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    void requestAction(Callback callback, boolean isShowPowerDialog) {
        Activity activity = getActivity();
        initialize();
        if (callback != null) mCallback = callback;
        boolean isAdminActive = mDevicePolicyManager.isAdminActive(mAdminReceiverComponentName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || isShowPowerDialog) {
            if (isAdminActive && !isShowPowerDialog) {
                // lockScreen & adminActive, auto remove admin.
                mDevicePolicyManager.removeActiveAdmin(mAdminReceiverComponentName);
            }
            if (!Utils.isAccessibilityServiceEnabled(getActivity(), PaService.class)) {
                try {
                    Log.d(TAG, "requestAction: Accessibility is enabled");
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } catch (ActivityNotFoundException e) {
                    // ActivityNotFoundException: Settings.ACTION_ACCESSIBILITY_SETTINGS
                    failed();
                }
            } else {
                // Send broadcast to show power dialog (21+) or to lock screen (28+).
                Intent intent = new Intent(isShowPowerDialog ? PaService.POWER_DIALOG_ACTION : PaService.LOCK_SCREEN_ACTION);
                intent.setPackage(activity.getPackageName());
                activity.sendBroadcast(intent);
                done();
            }
        } else {
            if (isAdminActive) {
                mDevicePolicyManager.lockNow();
                done();
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminReceiverComponentName);
                mRequestCode = new Random().nextInt();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
