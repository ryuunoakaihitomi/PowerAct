package demo.power_act;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

@SuppressWarnings("NullableProblems")
class DebugALC implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "DebugALC";

    DebugALC() {
        Log.d(TAG, "new instance: " + this);
    }

    @Override
    public void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "onActivityPreCreated: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPostCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "onActivityPostCreated: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPreStarted(Activity activity) {
        Log.d(TAG, "onActivityPreStarted: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, "onActivityStarted: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPostStarted(Activity activity) {
        Log.d(TAG, "onActivityPostStarted: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPreResumed(Activity activity) {
        Log.d(TAG, "onActivityPreResumed: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPostResumed(Activity activity) {
        Log.d(TAG, "onActivityPostResumed: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPrePaused(Activity activity) {
        Log.d(TAG, "onActivityPrePaused: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "onActivityPaused: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPostPaused(Activity activity) {
        Log.d(TAG, "onActivityPostPaused: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPreStopped(Activity activity) {
        Log.d(TAG, "onActivityPreStopped: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onActivityStopped: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPostStopped(Activity activity) {
        Log.d(TAG, "onActivityPostStopped: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPreSaveInstanceState(Activity activity, Bundle outState) {
        Log.d(TAG, "onActivityPreSaveInstanceState: " + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(TAG, "onActivitySaveInstanceState: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPostSaveInstanceState(Activity activity, Bundle outState) {
        Log.d(TAG, "onActivityPostSaveInstanceState: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPreDestroyed(Activity activity) {
        Log.d(TAG, "onActivityPreDestroyed: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "onActivityDestroyed: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPostDestroyed(Activity activity) {
        Log.d(TAG, "onActivityPostDestroyed: " + activity.getLocalClassName());
    }
}
