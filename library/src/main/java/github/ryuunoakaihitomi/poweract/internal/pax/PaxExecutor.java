package github.ryuunoakaihitomi.poweract.internal.pax;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.SystemService;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import github.ryuunoakaihitomi.poweract.BuildConfig;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.LibraryCompat;
import github.ryuunoakaihitomi.poweract.internal.util.SystemCompat;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;
import moe.shizuku.api.ShizukuSystemProperties;

class PaxExecutor {

    // The values of these tokens must be different from each other.
    static final String
            TOKEN_LOCK_SCREEN = "ls",
            TOKEN_REBOOT = "r",
            TOKEN_SHUTDOWN = "s",
            TOKEN_RECOVERY = "re",
            TOKEN_BOOTLOADER = "bl",
            TOKEN_SAFE_MODE = "sm",
            TOKEN_SOFT_REBOOT = "sr",
            TOKEN_KILL_SYSTEM_UI = "ksu";
    private static final String TAG = "PaxExecutor";

    private PaxExecutor() {
    }

    /**
     * @param args actionToken, forceMode, debugLog
     */
    public static void main(String[] args) {
        try {
            main0(args);
        } catch (Throwable throwable) {
            /* Explicit stacktrace output */
            DebugLog.e(TAG, "main: ", throwable);
            System.err.print(Log.getStackTraceString(throwable));
            throw throwable;
        }
    }

    private static void main0(String[] args) {
        if (args != null && args.length == 3 && !TextUtils.isEmpty(args[0])) {
            String token = args[0];
            boolean force = Boolean.parseBoolean(args[1]);
            if (Process.myUid() == Process.ROOT_UID) {
                DebugLog.enabled = Boolean.parseBoolean(args[2]);
                DebugLog.i(TAG, "main0: Run in root env.");
                SystemCompat.setPowerBinder(ServiceManager.getService(Context.POWER_SERVICE));
            }
            switch (token) {

                case TOKEN_LOCK_SCREEN:
                    SystemCompat.goToSleep();
                    break;

                case TOKEN_REBOOT:
                    if (force) {
                        SystemCompat.execShell("reboot");
                    } else {
                        SystemCompat.reboot(null);
                    }
                    break;

                case TOKEN_SHUTDOWN:
                    final String shutdownCmd = "reboot -p";
                    if (force) {
                        SystemCompat.execShell(shutdownCmd);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            SystemCompat.shutdown();
                        } else {
                            // Keep for test.
                            DebugLog.w(TAG, "main0: Cannot use PowerManager to shutdown before 17! Forcing...");
                            SystemCompat.execShell(shutdownCmd);
                        }
                    }
                    break;

                // setprop sys.powerctl reboot,recovery ?

                case TOKEN_RECOVERY:
                    if (force) {
                        SystemCompat.execShell("reboot recovery");
                    } else {
                        SystemCompat.reboot("recovery");
                    }
                    break;

                case TOKEN_BOOTLOADER:
                    if (force) {
                        SystemCompat.execShell("reboot bootloader");
                    } else {
                        SystemCompat.reboot("bootloader");
                    }
                    break;

                case TOKEN_SAFE_MODE:
                    //ShutdownThread.java
                    // Indicates whether we are rebooting into safe mode
                    @SuppressWarnings("SpellCheckingInspection") final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
                    final String one = "1";
                    if (force) {
                        SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, one);
                        SystemCompat.execShell("reboot");
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            SystemCompat.rebootSafeMode();
                        } else {
                            if (Process.myUid() != Process.ROOT_UID) {
                                // Test: Shizuku?
                                try {
                                    ShizukuSystemProperties.set(REBOOT_SAFEMODE_PROPERTY, one);
                                    if (ShizukuSystemProperties.getInt(REBOOT_SAFEMODE_PROPERTY, Integer.MIN_VALUE) != 1) {
                                        DebugLog.e(TAG, "main0: Shizuku: value not changed!");
                                        SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, one);
                                    }
                                } catch (Throwable e) {
                                    DebugLog.e(TAG, "main0: ShizukuSystemProperties", e);
                                    SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, one);
                                }
                            } else {
                                SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, one);
                            }
                            SystemCompat.reboot(null);
                        }
                    }
                    break;

                case TOKEN_SOFT_REBOOT:
                    // A new official solution "reboot userspace" is available since 30.
                    if (BuildConfig.DEBUG ||
                            // Seems a feasible solution of soft-rebooting by Shizuku.
                            LibraryCompat.isShizukuAvailable()) {
                        DebugLog.d(TAG, "main0: Testing... PowerManagerService.crash() == \"soft reboot\" ?");
                        SystemCompat.crash();
                        return;
                    }
                    SystemService.restart("zygote");
                    break;

                case TOKEN_KILL_SYSTEM_UI:
                    if (Process.myUid() != Process.ROOT_UID) {
                        throw new IllegalStateException("TOKEN_KILL_SYSTEM_UI must be called in root env!");
                    }

                    final String sysUiPkgName = "com.android.systemui";

                    /* Provide IActivityManager */
                    IActivityManager am = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            //noinspection JavaReflectionMemberAccess
                            am = (IActivityManager) ActivityManager.class.getMethod("getService").invoke(null);
                        } catch (Exception e) {
                            // IllegalAccessException | InvocationTargetException | NoSuchMethodException
                            DebugLog.e(TAG, "main0: TOKEN_KILL_SYSTEM_UI", e);
                        }
                    } else {
                        am = ActivityManagerNative.getDefault();
                    }

                    /* Kill system ui */
                    if (am != null) {
                        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                            if (info.processName.equals(sysUiPkgName)) {
                                int pid = info.pid;
                                DebugLog.i(TAG, "main0: Kill system ui. The process id is " + pid);
                                Process.killProcess(info.pid);
                                if (!BuildConfig.DEBUG) break;
                            }

                            /* Show all process info, instead of ps */
                            if (BuildConfig.DEBUG) {
                                DebugLog.d(TAG, "main: ---- RunningAppProcessInfo " + Arrays.asList(info.processName, info.pid));
                                Map<String, Object> infoMap = new TreeMap<>();
                                infoMap.put("uid", info.uid);
                                infoMap.put("pkgList", Arrays.toString(info.pkgList));
                                infoMap.put("importance", Utils.getClassIntApiConstantString(info.getClass(), "IMPORTANCE", info.importance));
                                infoMap.put("importanceReasonCode", Utils.getClassIntApiConstantString(info.getClass(), "REASON", info.importanceReasonCode));
                                infoMap.put("importanceReasonPid", info.importanceReasonPid);
                                infoMap.put("importanceReasonComponent", info.importanceReasonComponent != null ? info.importanceReasonComponent : "none");
                                infoMap.put("lru", info.lru);
                                infoMap.put("lastTrimLevel", Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                                        ? Utils.getClassIntApiConstantString(ComponentCallbacks2.class, "TRIM", info.lastTrimLevel)
                                        : "Unsupported before 16");
                                for (Map.Entry<String, Object> entry : infoMap.entrySet()) {
                                    DebugLog.d(TAG, "main0: " + entry.getKey() + " = " + entry.getValue());
                                }
                            }
                        }
                    } else {
                        throw new NullPointerException("am");
                    }
                    break;

                default:
                    throw new IllegalArgumentException("undefined token!");
            }
        } else {
            throw new IllegalArgumentException(args == null ? "null" : TextUtils.join(" ", args));
        }
    }
}