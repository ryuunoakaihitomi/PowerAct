package poweract.test.res;

import android.app.Application;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.ryuunoakaihitomi.retoast._Initializer;

/**
 * Not for outside.
 */
public class XposedMain implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.isFirstApplication) {
            hookAppCreate();
        }
    }

    private void hookAppCreate() {
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Application application = (Application) param.thisObject;
                application.registerActivityLifecycleCallbacks(DebugAlc.newInstance());
                _Initializer.main(null);
            }
        });
    }
}
