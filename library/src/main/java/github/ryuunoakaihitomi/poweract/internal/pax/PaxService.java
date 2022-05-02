package github.ryuunoakaihitomi.poweract.internal.pax;

import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.system.Os;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * A user service of Shizuku.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PaxService extends IPaxService.Stub {

    private static final String TAG = "PaxService";

    @Override
    public void killSystemUi() throws RemoteException {
        throwAsRemoteException(this::killSystemUiInternal);
    }

    private void killSystemUiInternal() {
        if (Process.myUid() != Process.ROOT_UID) {
            Log.e(TAG, "killSystemUi: Abort caused by illegal uid: " + Os.getuid());
            return;
        }
        PaxExecutor.killSystemUi();
        System.exit(0);
    }

    private void throwAsRemoteException(Runnable r) throws RemoteException {
        try {
            r.run();
        } catch (Throwable t) {
            throw new RemoteException("PaxService:\n" + Log.getStackTraceString(t));
        }
    }
}
