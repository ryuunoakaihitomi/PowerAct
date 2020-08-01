package demo.power_act;

import android.app.Application;
import android.os.StrictMode;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
    }
}
