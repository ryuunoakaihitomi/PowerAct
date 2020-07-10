package demo.power_act;

import android.app.Application;
import android.os.StrictMode;

import demo.power_act.utils.DebugALC;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(DebugALC.newInstance());
        StrictMode.enableDefaults();
    }
}
