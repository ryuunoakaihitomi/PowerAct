package github.ryuunoakaihitomi.poweract;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.Timer;
import java.util.TimerTask;

class UserGuideRunnable {

    public static final int RELEASE_DELAY_TIME_MILLIS = 5000;
    private static final String TAG = "UserGuideRunnable";
    @VisibleForTesting  // For testing, but invisible.
    private static Runnable sUserGuideRunnable;

    private UserGuideRunnable() {
    }

    static void set(@NonNull Runnable runnable) {
        sUserGuideRunnable = runnable;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                release();
            }
        }, RELEASE_DELAY_TIME_MILLIS);
    }

    static void run() {
        if (!Utils.isMainThread()) {
            DebugLog.i(TAG, "run: Not main thread. " + Thread.currentThread());
            new Handler(Looper.getMainLooper()).post(UserGuideRunnable::runInternal);
        } else {
            runInternal();
        }
    }

    private static void runInternal() {
        if (sUserGuideRunnable != null) {
            DebugLog.i(TAG, "runInternal: run");
            sUserGuideRunnable.run();
            release();
        } else {
            DebugLog.i(TAG, "runInternal: no-op");
        }
    }

    static void release() {
        DebugLog.d(TAG, "release: " + sUserGuideRunnable);
        sUserGuideRunnable = null;
    }
}
