package github.ryuunoakaihitomi.poweract;

import android.content.Context;
import android.os.Build;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import com.android.internal.os.Zygote;

class PaxCompat {

    private static final IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));

    private PaxCompat() {
    }

    static void goToSleep() {
        if (power == null) return;
        long uptimeMillis = SystemClock.uptimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            power.goToSleep(uptimeMillis, 0, 0);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            power.goToSleep(uptimeMillis, 0);
        } else {
            power.goToSleep(uptimeMillis);
        }
    }

    static void reboot(String reason) {
        if (power == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            power.reboot(false, reason, false);
        } else {
            power.reboot(reason);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    static void shutdown() {
        if (power == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            power.shutdown(false, null, false);
        } else {
            power.shutdown(false, false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    static void rebootSafeMode() {
        if (power == null) return;
        power.rebootSafeMode(false, false);
    }

    static void execShell(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Zygote.execShell(command);
        } else {
            dalvik.system.Zygote.execShell(command);
        }
    }

    static void crash() {
        if (power == null) return;
        power.crash("Soft reboot.");
    }
}
