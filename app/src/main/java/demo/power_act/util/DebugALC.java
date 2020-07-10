package demo.power_act.util;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DebugALC {

    private static final String TAG = "DebugALC";

    private DebugALC() {
        Log.d(TAG, "new instance: " + this);
    }

    public static Application.ActivityLifecycleCallbacks newInstance() {
        Class<?> alc = Application.ActivityLifecycleCallbacks.class;
        return (Application.ActivityLifecycleCallbacks) Proxy.newProxyInstance(
                alc.getClassLoader(), new Class[]{alc}, new Handler());
    }

    private static class Handler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            Activity activity = (Activity) args[0];
            Log.d(TAG, activity.getLocalClassName() + " -> " + method.getName());
            return null;
        }
    }
}
