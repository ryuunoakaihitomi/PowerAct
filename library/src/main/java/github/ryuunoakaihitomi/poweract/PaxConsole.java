package github.ryuunoakaihitomi.poweract;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

class PaxConsole {

    private static final String TAG = "PaxConsole";

    private static PaxInterface sInterface;

    private PaxConsole() {
    }

    public static PaxInterface getInterface() {
        if (sInterface == null) {
            DebugLog.d(TAG, "getInterface: Initializing interface...");
            ClassLoader loader = PaxInterface.class.getClassLoader();
            Class<?>[] interfaces = new Class[]{PaxInterface.class};
            InvocationHandler handler = new PaxHandler();
            sInterface = (PaxInterface) Proxy.newProxyInstance(loader, interfaces, handler);
        }
        return sInterface;
    }
}
