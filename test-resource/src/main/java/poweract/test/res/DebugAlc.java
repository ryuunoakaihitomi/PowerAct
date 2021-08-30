package poweract.test.res;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @see android.app.Application.ActivityLifecycleCallbacks
 */
class DebugAlc {

    private static final String TAG = "DebugAlc";

    private DebugAlc() {
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
            Object firstArg = args[0];
            if (firstArg instanceof Activity) {
                Activity activity = (Activity) firstArg;
                Log.d(TAG, activity.getComponentName().getClassName() + " -> " + method.getName());
            }
            // interface!!!
            //noinspection SuspiciousInvocationHandlerImplementation
            return null;
        }
    }
}
