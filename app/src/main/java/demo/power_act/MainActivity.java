package demo.power_act;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.ExternalUtils;
import github.ryuunoakaihitomi.poweract.PowerAct;
import github.ryuunoakaihitomi.poweract.PowerActX;
import github.ryuunoakaihitomi.poweract.PowerButton;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button lockScreenBtn = findViewById(R.id.lockScreenBtn);
        Button powerDialogBtn = findViewById(R.id.powerDialogBtn);

        // Create callback object
        final Callback callback = new Callback() {

            @Override
            public void done() {
                Log.i(TAG, "done: PowerAct cb DONE! -------");
                finish();
            }

            @Override
            public void failed() {
                Log.e(TAG, "failed: PowerAct cb FAILED! *******");
                Toast.makeText(getApplicationContext(), "Denied", Toast.LENGTH_SHORT).show();
            }
        };

        Activity activity = this;

        // PowerAct examples
        lockScreenBtn.setOnClickListener(v -> {
            // Lock screen, without callback.
            PowerAct.lockScreen(activity);
        });
        powerDialogBtn.setOnClickListener(v -> {
            // Show system power dialog, with callback.
            PowerAct.showPowerDialog(activity, callback);
        });
        // An additional widget.
        PowerButton powerButton = findViewById(R.id.pwrBtn);

        powerButton.setOnLongClickListener(v -> {
            Log.d(TAG, "onLongClick: pwrBtn");
            return false;
        });

        // PowerActX examples
        /*
         * Usage:
         * PowerActX.<action>()
         * PowerActX.<action>(callback)
         * PowerActX.<action>(force)
         * PowerActX.<action>(callback, force)
         */
        setXButtonAction(R.id.ls_btn, () -> PowerActX.lockScreen(callback), null);
        setXButtonAction(R.id.rb_btn, () -> PowerActX.reboot(callback), () -> PowerActX.reboot(callback, true));
        setXButtonAction(R.id.sd_btn, () -> PowerActX.shutdown(callback), () -> PowerActX.shutdown(callback, true));
        setXButtonAction(R.id.rec_btn, () -> PowerActX.recovery(callback), () -> PowerActX.recovery(callback, true));
        setXButtonAction(R.id.bl_btn, () -> PowerActX.bootloader(callback), () -> PowerActX.bootloader(callback, true));
        setXButtonAction(R.id.sm_btn, () -> PowerActX.safeMode(callback), () -> PowerActX.safeMode(callback, true));
        setXButtonAction(R.id.srb_btn, () -> PowerActX.softReboot(callback), null);

        findViewById(R.id.disComBtn).setOnClickListener(v ->
                // Disable exposed components manually.
                ExternalUtils.disableExposedComponents(getApplicationContext()));

        // Control logcat output.
        ExternalUtils.enableLog(true);
    }

    private void setXButtonAction(int btnResId, Runnable action, Runnable longClickAction) {
        Button button = findViewById(btnResId);
        button.setOnClickListener(v -> action.run());
        if (longClickAction != null) {
            button.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }
    }
}
