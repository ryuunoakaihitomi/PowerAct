package demo.power_act;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import github.ryuunoakaihitomi.poweract.Callback;
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

        // Lock screen, Without callback.
        lockScreenBtn.setOnClickListener(v -> PowerAct.lockScreen(MainActivity.this));
        // Show system power dialog, with callback.
        powerDialogBtn.setOnClickListener(v -> PowerAct.showPowerDialog(MainActivity.this, callback));
        // An additional widget.
        PowerButton powerButton = findViewById(R.id.pwrBtn);

        powerButton.setOnLongClickListener(v -> {
            Log.d(TAG, "onLongClick: pwrBtn");
            return false;
        });

    }
}
