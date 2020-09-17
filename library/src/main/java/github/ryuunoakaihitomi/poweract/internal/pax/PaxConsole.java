package github.ryuunoakaihitomi.poweract.internal.pax;

import androidx.annotation.CheckResult;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;

public final class PaxConsole {

    private static final String TAG = "PaxConsole";

    private static volatile PaxInterface sInterface;

    private PaxConsole() {
    }

    @CheckResult
    public static PaxInterface getInterface() {
        if (sInterface == null) {
            synchronized (PaxConsole.class) {
                if (sInterface == null) {
                    DebugLog.d(TAG, "getInterface: Initializing interface...");
                    ClassLoader loader = PaxInterface.class.getClassLoader();
                    Class<?>[] interfaces = new Class[]{PaxInterface.class};
                    InvocationHandler handler = new PaxHandler();
                    sInterface = (PaxInterface) Proxy.newProxyInstance(loader, interfaces, handler);
                }
            }
        }
        return sInterface;
    }
}
