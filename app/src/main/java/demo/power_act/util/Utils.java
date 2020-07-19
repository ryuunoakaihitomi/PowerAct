package demo.power_act.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final String TAG = "Utils";

    private Utils() {
    }

    public static String timestamp2String(String format, long timestamp) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date(timestamp));
    }


    /**
     * To fix 2 bugs in {@link Toast}.
     *
     * <li>
     *     <ul>Will not be shown when the notification permission has benn disabled before {@link Build.VERSION_CODES#Q}.</ul>
     *     <ul>In {@link Build.VERSION_CODES#N_MR1}, tha app will crash while there's a blocking op after {@link Toast#show()}.</ul>
     * </li>
     *
     * @see <a href="https://github.com/PureWriter/ToastCompat">ToastCompat</a>
     * @see <a href="https://blog.csdn.net/qq331710168/article/details/85320098">解决通知关闭Toast失效问题</a>
     */
    public static void toastBugFix() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                @SuppressLint("DiscouragedPrivateApi")
                Method getService = Toast.class.getDeclaredMethod("getService");
                getService.setAccessible(true);
                // Do not be inline to avoid endless loop.
                Object iNotificationManager = getService.invoke(null);
                @SuppressLint("PrivateApi")
                Object iNotificationManagerProxy = Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{Class.forName("android.app.INotificationManager")},
                        (proxy, method, args) -> {
                            if ("enqueueToast".equals(method.getName())) {
                                Log.d(TAG, "defineSystemToast: " + Arrays.toString(args));
                                args[0] = "android";
                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                    Object tn = args[1];
                                    Field mHandler = tn.getClass().getDeclaredField("mHandler");
                                    mHandler.setAccessible(true);
                                    final class HandlerProxy extends Handler {
                                        private Handler mHandler;

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
                                                Log.i(TAG, "handleMessage: BTE!", e);
                                            }
                                        }
                                    }
                                    mHandler.set(tn, new HandlerProxy((Handler) mHandler.get(tn)));
                                }
                            }
                            return method.invoke(iNotificationManager, args);
                        });
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field sService = Toast.class.getDeclaredField("sService");
                sService.setAccessible(true);
                sService.set(null, iNotificationManagerProxy);
            } catch (Exception e) {
                Log.e(TAG, "defineSystemToast: ", e);
            }
        }
    }
}
