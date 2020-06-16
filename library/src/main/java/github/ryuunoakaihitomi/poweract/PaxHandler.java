package github.ryuunoakaihitomi.poweract;

import android.app.ActivityThread;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executors;

class PaxHandler implements InvocationHandler {

    private static final String TAG = "PaxHandler";

    private static final Application sApplication;

    static {
        DebugLog.d(TAG, "static initializer");
        sApplication = ActivityThread.currentApplication();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Callback callback = (Callback) args[0];
        if (callback == null) {
            callback = () -> DebugLog.d(TAG, "invoke: normal callback: FAILED!");
        }
        boolean force = false;
        if (args.length == 2) force = (boolean) args[1];
        final String forceString = Boolean.toString(force);
        PaxExecApi cmdList = method.getAnnotation(PaxExecApi.class);
        final Callback finalCallback = callback;
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            if (cmdList != null) {
                DebugLog.d(TAG, "invoke: cmd " + Arrays.asList(cmdList, forceString));
                boolean returnValue =
                        Utils.runSuJavaWithAppProcess(sApplication,
                                PaxExecutor.class,
                                cmdList.value(), forceString);
                if (returnValue) mainHandler.post(() -> {
                    ExternalUtils.disableExposedComponents(sApplication);
                    finalCallback.done();
                });
                else mainHandler.post(finalCallback::failed);
            } else {
                mainHandler.post(finalCallback::failed);
            }
        });
        // void
        return null;
    }
}
