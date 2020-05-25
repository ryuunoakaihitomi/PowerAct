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
import android.view.accessibility.AccessibilityManager;

import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("deprecation")
public class PaFragment extends Fragment {

    private static final String TAG = "PaFragment";
    private int mRequestCode;
    // don't be null.
    private Callback mCallback = new Callback() {

        @Override
        public void done() {
            Log.d(TAG, "done: Remember to see what happened.");
        }

        @Override
        public void failed() {
            Log.d(TAG, "failed: !");
        }
    };

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminCom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminCom = new ComponentName(getActivity(), AdminReceiver.class);
        Log.d(TAG, "onCreate: initialized... " + Arrays.asList(mDevicePolicyManager, mAdminCom));
    }

    void requestAction(Callback callback, boolean isShowPowerDialog) {
        Activity activity = getActivity();
        mRequestCode = new Random().nextInt();
        if (callback != null) mCallback = callback;
        boolean isAdminActive = mDevicePolicyManager.isAdminActive(mAdminCom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || isShowPowerDialog) {
            if (isAdminActive && !isShowPowerDialog) {
                // lockScreen & adminActive, auto remove admin.
                mDevicePolicyManager.removeActiveAdmin(mAdminCom);
            }
            AccessibilityManager manager = (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (manager != null) {
                if (!manager.isEnabled()) {
                    try {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    } catch (ActivityNotFoundException e) {
                        // ActivityNotFoundException: Settings.ACTION_ACCESSIBILITY_SETTINGS
                        mCallback.failed();
                    }
                } else {
                    // Send broadcast to show power dialog (21+) or to lock screen (28+).
                    Intent intent = new Intent(isShowPowerDialog ? PaService.POWER_DIALOG_ACTION : PaService.LOCK_SCREEN_ACTION);
                    intent.setPackage(activity.getPackageName());
                    activity.sendBroadcast(intent);
                    mCallback.done();
                }
            } else {
                // AccessibilityManager == null
                mCallback.failed();
            }
        } else {
            if (isAdminActive) {
                mDevicePolicyManager.lockNow();
                mCallback.done();
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminCom);
                try {
                    startActivityForResult(intent, mRequestCode);
                } catch (ActivityNotFoundException e) {
                    // ActivityNotFoundException: DevicePolicyManager.EXTRA_DEVICE_ADMIN
                    mCallback.failed();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mRequestCode == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                if (mDevicePolicyManager.isAdminActive(mAdminCom)) {
                    mDevicePolicyManager.lockNow();
                    mCallback.done();
                }
            } else {
                // resultCode != Activity.RESULT_OK
                mCallback.failed();
            }
        } else {
            // mRequestCode != requestCode
            mCallback.failed();
        }
    }
}
