package github.ryuunoakaihitomi.poweract;

import android.content.Context;
import android.os.Build;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import com.android.internal.os.Zygote;

class PaxCompat {

    private static IPowerManager pm;

    private PaxCompat() {
    }

    static void initializePowerService() {
        pm = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
    }

    static void goToSleep() {
        long uptimeMillis = SystemClock.uptimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pm.goToSleep(uptimeMillis, 0, 0);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            pm.goToSleep(uptimeMillis, 0);
        } else {
            pm.goToSleep(uptimeMillis);
        }
    }

    static void reboot(String reason) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            pm.reboot(false, reason, false);
        } else {
            pm.reboot(reason);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    static void shutdown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pm.shutdown(false, null, false);
        } else {
            pm.shutdown(false, false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    static void rebootSafeMode() {
        pm.rebootSafeMode(false, false);
    }

    static void execShell(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Zygote.execShell(command);
        } else {
            dalvik.system.Zygote.execShell(command);
        }
    }
}
