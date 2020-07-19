package demo.power_act;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import demo.power_act.util.DebugAlc;
import demo.power_act.util.Utils;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(DebugAlc.newInstance());
        StrictMode.enableDefaults();
    }

    @Override
    protected void attachBaseContext(Context base) {
        Utils.toastBugFix();
        super.attachBaseContext(base);
    }
}
