package dalvik.system;


/**
 * Provides access to the Dalvik "zygote" feature, which allows a VM instance to
 * be partially initialized and then fork()'d from the partially initialized
 * state.
 */
// https://android.googlesource.com/platform/libcore/+/refs/heads/ics-factoryrom-2-release/dalvik/src/main/java/dalvik/system/Zygote.java
@SuppressWarnings("unused")
public final class Zygote {

    /**
     * Executes "/system/bin/sh -c &lt;command&gt;" using the exec() system call.
     * This method throws a runtime exception if exec() failed, otherwise, this
     * method never returns.
     *
     * @param command The shell command to execute.
     */
    public static void execShell(String command) {
    }
}
