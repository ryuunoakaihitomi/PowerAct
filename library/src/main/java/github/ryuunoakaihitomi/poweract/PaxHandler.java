package github.ryuunoakaihitomi.poweract;

import android.app.ActivityThread;
import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        Executors.newSingleThreadExecutor().execute(() -> {
            if (cmdList != null) {
                DebugLog.d(TAG, "invoke: cmd " + Arrays.asList(cmdList, forceString));
                final ScheduledExecutorService guideExecutor = Executors.newSingleThreadScheduledExecutor();
                guideExecutor.schedule(UserGuideRunnable::run, USER_GUIDE_DELAY_TIME_MILLIS, TimeUnit.MILLISECONDS);
                boolean returnValue =
                        Utils.runSuJavaWithAppProcess(sApplication,
                                PaxExecutor.class,
                                cmdList.value(), forceString);
                if (returnValue) mainHandler.post(() -> {
                    cancelUserGuide(guideExecutor);
                    ExternalUtils.disableExposedComponents(sApplication);
                    callbackHelper.done();
                });
                else mainHandler.post(() -> {
                    cancelUserGuide(guideExecutor);
                    callbackHelper.failed();
                });
            } else {
                mainHandler.post(callbackHelper::failed);
            }
        });
        // void
        return null;
    }

    private void cancelUserGuide(final ScheduledExecutorService guideExecutor) {
        guideExecutor.shutdownNow();
        UserGuideRunnable.release();
    }
}
