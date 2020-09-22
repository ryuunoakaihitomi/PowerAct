package github.ryuunoakaihitomi.poweract.internal.pax;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.VisibleForTesting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
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
import moe.shizuku.api.ShizukuBinderWrapper;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.SystemServiceHelper;

final class PaxHandler implements InvocationHandler {

    private static final String TAG = "PaxHandler";

    private static final Application sApplication;

    @VisibleForTesting
    public static final int USER_GUIDE_DELAY_TIME_MILLIS = 3000;

    static {
        DebugLog.d(TAG, "static initializer");
        sApplication = ActivityThread.currentApplication();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        final CallbackHelper callbackHelper = CallbackHelper.of((Callback) args[0]);
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 && !Utils.isMainThread()) {
            DebugLog.e(TAG, "invoke: Must be called in main thread before 18! application = " + sApplication);
            mainHandler.post(callbackHelper::failed);
            return null;
        }
        boolean force = false;
        if (args.length == 2) force = (boolean) args[1];
        final String forceString = Boolean.toString(force);
        PaxExecApi cmdList = method.getAnnotation(PaxExecApi.class);
        // Should not happen!
        if (cmdList == null) {
            DebugLog.e(TAG, "invoke: annotation?");
            mainHandler.post(callbackHelper::failed);
            return null;
        }
        final String cmd = cmdList.value();

        boolean shizukuSuccess = false;
        int shizukuServiceUid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? Process.INVALID_UID : Integer.MIN_VALUE;
        if (LibraryCompat.isShizukuPrepared(sApplication)) {
            try {
                shizukuServiceUid = ShizukuService.getUid();
                // Must be called in root env! Using adb can only lock screen, it doesn't make sense in PowerActX. (svc)
                // Force mode uses shell, not PowerManager.
                if (shizukuServiceUid == Process.ROOT_UID && !force) {
                    SystemCompat.setPowerBinder(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.POWER_SERVICE)));
                    // Exception: kill sys ui.
                    if (PaxExecutor.TOKEN_KILL_SYSTEM_UI.equals(cmd)) {
                        DebugLog.i(TAG, "invoke: Cannot kill SysUi without root shell!");
                    } else {
                        PaxExecutor.main(new String[]{cmd, forceString, String.valueOf(DebugLog.enabled)});
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

        DebugLog.w(TAG, "invoke: Use alternative solution without Shizuku. args = " + Arrays.asList(shizukuServiceUid, force, cmd));
        Executors.newSingleThreadExecutor().execute(() -> {
            DebugLog.d(TAG, "invoke: cmd " + Arrays.asList(cmdList, forceString));
            final ScheduledExecutorService guideExecutor = Executors.newSingleThreadScheduledExecutor();
            guideExecutor.schedule(UserGuideRunnable::run, USER_GUIDE_DELAY_TIME_MILLIS, TimeUnit.MILLISECONDS);
            boolean returnValue = Utils.runSuJavaWithAppProcess(sApplication, PaxExecutor.class, cmd, forceString, String.valueOf(DebugLog.enabled));
            if (returnValue) mainHandler.post(() -> {
                cancelUserGuide(guideExecutor);
                ExternalUtils.disableExposedComponents(sApplication);
                callbackHelper.done();
            });
            else mainHandler.post(() -> {
                cancelUserGuide(guideExecutor);
                callbackHelper.failed();
            });
        });
        // void
        return null;
    }

    private void cancelUserGuide(final ScheduledExecutorService guideExecutor) {
        guideExecutor.shutdownNow();
        UserGuideRunnable.release();
    }
}
