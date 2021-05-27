package github.ryuunoakaihitomi.poweract.internal.util;

import android.os.Build;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import com.android.internal.os.Zygote;

public class SystemCompat {

    private static IPowerManager power;

    private SystemCompat() {
    }

    public static void setPowerBinder(IBinder binder) {
        power = IPowerManager.Stub.asInterface(binder);
    }

    public static void goToSleep() {
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

    public static void reboot(String reason) {
        if (power == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            power.reboot(false, reason, false);
        } else {
            power.reboot(reason);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void shutdown() {
        if (power == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            power.shutdown(false, null, false);
        } else {
            power.shutdown(false, false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void rebootSafeMode() {
        if (power == null) return;
        power.rebootSafeMode(false, false);
    }

    public static void execShell(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Zygote.execShell(command);
        } else {
            dalvik.system.Zygote.execShell(command);
        }
    }

    public static void crash() {
        // Make a crash in system server thread will cause a soft reboot.
        if (power == null) throw new NullPointerException("power binder");
        power.crash("Soft reboot.");
    }
}
