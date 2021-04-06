// IWindowManager.aidl
package android.view;

import android.annotation.TargetApi;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import static android.os.Build.VERSION_CODES.R;

/**
 * System private interface to the window manager.
 */
public interface IWindowManager extends IInterface {

    /**
     * Called to show global actions.
     */
    @TargetApi(R)
    void showGlobalActions();

    @SuppressWarnings({"UnnecessaryInterfaceModifier", "unused"})
    public abstract static class Stub extends Binder implements IWindowManager {

        public static IWindowManager asInterface(IBinder var0) {
            return null;
        }
    }
}
