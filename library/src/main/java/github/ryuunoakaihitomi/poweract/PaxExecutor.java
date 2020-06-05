package github.ryuunoakaihitomi.poweract;

import android.os.Build;
import android.os.SystemProperties;
import android.os.SystemService;
import android.text.TextUtils;
import android.util.Log;

class PaxExecutor {

    // The values of these tokens must be different from each other.
    static final String
            TOKEN_LOCK_SCREEN = "ls",
            TOKEN_REBOOT = "r",
            TOKEN_SHUTDOWN = "s",
            TOKEN_RECOVERY = "re",
            TOKEN_BOOTLOADER = "bl",
            TOKEN_SAFE_MODE = "sm",
            TOKEN_SOFT_REBOOT = "sr";
    private static final String TAG = "PaxExecutor";

    private PaxExecutor() {
    }

    public static void main(String[] args) {
        if (args != null && args.length == 2 && !TextUtils.isEmpty(args[0])) {
            String token = args[0];
            boolean force = Boolean.parseBoolean(args[1]);
            switch (token) {

                case TOKEN_LOCK_SCREEN:
                    PaxCompat.goToSleep();
                    break;

                case TOKEN_REBOOT:
                    if (force) {
                        PaxCompat.execShell("reboot");
                    } else {
                        PaxCompat.reboot(null);
                    }
                    break;

                case TOKEN_SHUTDOWN:
                    final String shutdownCmd = "reboot -p";
                    if (force) {
                        PaxCompat.execShell(shutdownCmd);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            PaxCompat.shutdown();
                        } else {
                            // Keep for test.
                            Log.w(TAG, "main: Cannot use pm to shutdown before 17! Forcing...");
                            PaxCompat.execShell(shutdownCmd);
                        }
                    }
                    break;

                // setprop sys.powerctl reboot,recovery ?

                case TOKEN_RECOVERY:
                    if (force) {
                        PaxCompat.execShell("reboot recovery");
                    } else {
                        PaxCompat.reboot("recovery");
                    }
                    break;

                case TOKEN_BOOTLOADER:
                    if (force) {
                        PaxCompat.execShell("reboot bootloader");
                    } else {
                        PaxCompat.reboot("bootloader");
                    }
                    break;

                case TOKEN_SAFE_MODE:
                    //ShutdownThread.java
                    // Indicates whether we are rebooting into safe mode
                    @SuppressWarnings("SpellCheckingInspection") final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
                    if (force) {
                        SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, "1");
                        PaxCompat.execShell("reboot");
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            PaxCompat.rebootSafeMode();
                        } else {
                            SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, "1");
                            PaxCompat.reboot(null);
                        }
                    }
                    break;

                case TOKEN_SOFT_REBOOT:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "main: Testing... PowerManagerService.crash() == \"soft reboot\" ?");
                        PaxCompat.crash();
                        return;
                    }
                    SystemService.restart("zygote");
                    break;

                default:
                    throw new IllegalArgumentException("undefined token!");
            }
        } else {
            throw new IllegalArgumentException(args == null ? "null" : TextUtils.join(" ", args));
        }
    }
}
