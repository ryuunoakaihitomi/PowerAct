package demo.power_act;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.ExternalUtils;
import github.ryuunoakaihitomi.poweract.PowerAct;
import github.ryuunoakaihitomi.poweract.PowerButton;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getApplication().registerActivityLifecycleCallbacks(new DebugALC());

        Button lockScreenBtn = findViewById(R.id.lockScreenBtn);
        Button powerDialogBtn = findViewById(R.id.powerDialogBtn);

        final Callback callback = new Callback() {

            @Override
            public void done() {
                finish();
            }

            @Override
            public void failed() {
                Toast.makeText(getApplicationContext(), "Denied", Toast.LENGTH_SHORT).show();
            }
        };

        Activity activity = this;

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

        // PowerActX
        PowerActXTest.setLockScreenActionButton(findViewById(R.id.ls_btn));
        PowerActXTest.setRebootActionButton(findViewById(R.id.rb_btn));
        PowerActXTest.setShutdownActionButton(findViewById(R.id.sd_btn));
        PowerActXTest.setRecoveryActionButton(findViewById(R.id.rec_btn));
        PowerActXTest.setBootloaderActionButton(findViewById(R.id.bl_btn));
        PowerActXTest.setSafeModeActionButton(findViewById(R.id.sm_btn));
        PowerActXTest.setSoftRebootActionButton(findViewById(R.id.srb_btn));

        findViewById(R.id.disComBtn).setOnClickListener(v ->
                // Disable exposed components manually.
                ExternalUtils.disableExposedComponents(getApplicationContext()));
    }
}
