package poweract.test.res;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * To fix 2 bugs in {@link Toast}.
 *
 * <li>
 *     <ul>Will not be shown when the notification permission has benn disabled before {@link Build.VERSION_CODES#Q}.</ul>
 *     <ul>In {@link Build.VERSION_CODES#N_MR1}, tha app will crash while there's a blocking op after {@link Toast#show()}.</ul>
 * </li>
 *
 * @see #$()
 */
class ToastBugFix {

    private static final String TAG = "ToastBugFix";

    private ToastBugFix() {
    }

    public static void $() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
                Method getService = Toast.class.getDeclaredMethod("getService");
                getService.setAccessible(true);
                // Do not be inline to avoid endless loop.
                final Object iNotificationManager = getService.invoke(null);
                @SuppressLint("PrivateApi")
                Object iNotificationManagerProxy = Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{Class.forName("android.app.INotificationManager")},
                        new InvocationHandler() {

                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if ("enqueueToast".equals(method.getName())) {
                                    // duration: LENGTH_SHORT = 0; LENGTH_LONG = 1;
                                    Log.d(TAG, "pkg = " + args[0] + ", duration = " + args[2]);
                                    args[0] = "android";
                                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                        Object tn = args[1];
                                        Field mHandler = tn.getClass().getDeclaredField("mHandler");
                                        mHandler.setAccessible(true);
                                        final class HandlerProxy extends Handler {
                                            private final Handler mHandler;

                                            @SuppressWarnings("deprecation") // since 30
                                            public HandlerProxy(Handler handler) {
                                                mHandler = handler;
                                            }

                                            @Override
                                            public void handleMessage(Message msg) {
                                                // SHOW = 0; HIDE = 1; CANCEL = 2;
                                                Log.d(TAG, "handleMessage: action = " + msg.what);
                                                try {
                                                    mHandler.handleMessage(msg);
                                                } catch (WindowManager.BadTokenException e) {
                                                    Log.i(TAG, "handleMessage: BTE caught!", e);
                                                }
                                            }
                                        }
                                        mHandler.set(tn, new HandlerProxy((Handler) mHandler.get(tn)));
                                    }
                                }
                                return method.invoke(iNotificationManager, args);
                            }
                        });
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field sService = Toast.class.getDeclaredField("sService");
                sService.setAccessible(true);
                sService.set(null, iNotificationManagerProxy);
            } catch (Exception e) {
                Log.e(TAG, null, e);
            }
        }
    }
}
