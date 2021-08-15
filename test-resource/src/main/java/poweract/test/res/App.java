package poweract.test.res;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

public class App extends Application {

    private static final String TAG = "App";

    @Override
    protected void attachBaseContext(Context base) {
        installMultiDex(base);
        callHiddenApiBypass();
        super.attachBaseContext(base);
    }

    private void installMultiDex(Context context) {
        try {
            Class<?> multiDex = Class.forName("androidx.multidex.MultiDex");
            Method install = multiDex.getMethod("install", Context.class);
            install.invoke(null, context);
        } catch (Exception e) {
            Log.v(TAG, "installMultiDex: " + e);
        }
    }

    public static void callHiddenApiBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                //noinspection SpellCheckingInspection
                Class.forName("org.lsposed.hiddenapibypass.HiddenApiBypass")
                        .getMethod("addHiddenApiExemptions", String[].class)
                        .invoke(null, (Object) new String[]{""});
            } catch (ReflectiveOperationException e) {
                Log.v(TAG, "callHiddenApiBypass: " + e);
            }
        }
    }
}
