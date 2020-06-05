package demo.power_act;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.PowerActX;

class PowerActXTest {

    private static final String TAG = "PowerActXTest";

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    private static final Callback sLogCallback = new Callback() {
        @Override
        public void done() {
            Log.i(TAG, "done: PowerActXTest cb DONE! -------");
        }

        @Override
        public void failed() {
            if (sContext != null) {
                Toast.makeText(sContext, "PowerActXTest: ?", Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "failed: PowerActXTest cb FAILED! *******");
        }
    };

    private PowerActXTest() {
    }

    static void setLockScreenActionButton(Button button) {
        sContext = button.getContext().getApplicationContext();
        button.setOnClickListener(v -> PowerActX.lockScreen(sLogCallback));
    }

    static void setRebootActionButton(Button button) {
        button.setOnClickListener(v -> PowerActX.reboot(sLogCallback));
        button.setOnLongClickListener(v -> {
            PowerActX.reboot(sLogCallback, true);
            return true;
        });
    }

    static void setShutdownActionButton(Button button) {
        button.setOnClickListener(v -> PowerActX.shutdown(sLogCallback));
        button.setOnLongClickListener(v -> {
            PowerActX.shutdown(sLogCallback, true);
            return true;
        });
    }

    static void setRecoveryActionButton(Button button) {
        button.setOnClickListener(v -> PowerActX.recovery(sLogCallback));
        button.setOnLongClickListener(v -> {
            PowerActX.recovery(sLogCallback, true);
            return true;
        });
    }

    static void setBootloaderActionButton(Button button) {
        button.setOnClickListener(v -> PowerActX.bootloader(sLogCallback));
        button.setOnLongClickListener(v -> {
            PowerActX.bootloader(sLogCallback, true);
            return true;
        });
    }

    static void setSafeModeActionButton(Button button) {
        button.setOnClickListener(v -> PowerActX.safeMode(sLogCallback));
        button.setOnLongClickListener(v -> {
            PowerActX.safeMode(sLogCallback, true);
            return true;
        });
    }

    static void setSoftRebootActionButton(Button button) {
        button.setOnClickListener(v -> PowerActX.softReboot(sLogCallback));
    }
}
