package github.ryuunoakaihitomi.poweract.internal.pax;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.ExternalUtils;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.LibraryCompat;
import github.ryuunoakaihitomi.poweract.internal.util.SystemCompat;
import github.ryuunoakaihitomi.poweract.internal.util.UserGuideRunnable;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

final class PaxHandler implements InvocationHandler {

    private static final String TAG = "PaxHandler";

    private static final Application sApplication;

    @VisibleForTesting
    public static final int USER_GUIDE_DELAY_TIME_MILLIS = 3000;

    static {
        DebugLog.d(TAG, "static initializer");
        sApplication = ActivityThread.currentApplication();
    }

    @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        final CallbackHelper callbackHelper = CallbackHelper.of((Callback) args[0]);
        final boolean force = (boolean) Utils.arraySafeGet(args, 1, false);
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final PaxExecApi cmdList = method.getAnnotation(PaxExecApi.class);
        // Should not happen! Keep for NPE lint of cmdList.value().
        if (cmdList == null) {
            DebugLog.e(TAG, "invoke: annotation?");
            mainHandler.post(callbackHelper::failed);
            return null;
        }
        String cmd = cmdList.value();
        // Special case for custom reboot.
        if (PaxExecutor.TOKEN_CUSTOM_REBOOT.equals(cmd)) {
            String rebootArg = (String) Utils.arraySafeGet(args, 2, null);
            if (!TextUtils.isEmpty(rebootArg)) cmd += "_" + rebootArg;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 && !Utils.isMainThread()) {
            DebugLog.e(TAG, "invoke: Must be called in main thread before 18! application = " + sApplication);
            mainHandler.post(callbackHelper::failed);
            return null;
        }
        if (ActivityManager.isUserAMonkey() && !PaxExecutor.TOKEN_LOCK_SCREEN.equals(cmd)) {
            DebugLog.e(TAG, "invoke: Reject monkey except for lock screen.");
            mainHandler.post(callbackHelper::failed);
            return null;
        }

        final String enableLog = String.valueOf(DebugLog.enabled);
        final String forceString = Boolean.toString(force);

        boolean shizukuSuccess = false;
        int shizukuServiceUid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? Process.INVALID_UID : Integer.MIN_VALUE;
        if (LibraryCompat.isShizukuPrepared()) {
            try {
                shizukuServiceUid = Shizuku.getUid();
                // Must be called in root env! Using adb can only lock screen, it doesn't make sense in PowerActX. (svc)
                // Force mode uses shell, not PowerManager.
                if (shizukuServiceUid == Process.ROOT_UID && !force) {
                    SystemCompat.setPowerBinder(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.POWER_SERVICE)));
                    // Exception: kill sys ui.
                    if (PaxExecutor.TOKEN_KILL_SYSTEM_UI.equals(cmd)) {
                        DebugLog.i(TAG, "invoke: Cannot kill SysUi without root shell!");
                    } else {
                        // "enableLog" is useless here, only as a placeholder.
                        PaxExecutor.main(new String[]{cmd, forceString, enableLog});
                        shizukuSuccess = true;
                    }
                }
            } catch (Throwable e) {
                // Exception from ShizukuService | PaxExecutor
                DebugLog.e(TAG, "invoke: by Shizuku", e);
            }
        }
        if (shizukuSuccess) {
            mainHandler.post(() -> {
                ExternalUtils.disableExposedComponents(sApplication);
                callbackHelper.done();
                UserGuideRunnable.release();
            });
            return null;
        }

        DebugLog.w(TAG, "invoke: Use alternative solution without Shizuku. args = " + Arrays.asList(shizukuServiceUid, force, cmd, enableLog));
        String finalCmd = cmd;
        final ExecutorService mainExecutor = Executors.newSingleThreadExecutor();
        mainExecutor.execute(() -> {
            DebugLog.d(TAG, "invoke: cmd " + Arrays.asList(cmdList, forceString, enableLog));
            final ScheduledExecutorService guideExecutor = Executors.newSingleThreadScheduledExecutor();
            guideExecutor.schedule(UserGuideRunnable::run, USER_GUIDE_DELAY_TIME_MILLIS, TimeUnit.MILLISECONDS);
            boolean returnValue = Utils.runSuJavaWithAppProcess(sApplication, PaxExecutor.class, finalCmd, forceString, enableLog);
            if (returnValue) mainHandler.post(() -> {
                cancelUserGuide(guideExecutor);
                ExternalUtils.disableExposedComponents(sApplication);
                callbackHelper.done();
            });
            else mainHandler.post(() -> {
                cancelUserGuide(guideExecutor);
                callbackHelper.failed();
            });
            mainExecutor.shutdown();
        });
        // void
        return null;
    }

    private void cancelUserGuide(final ScheduledExecutorService guideExecutor) {
        guideExecutor.shutdownNow();
        UserGuideRunnable.release();
    }
}
