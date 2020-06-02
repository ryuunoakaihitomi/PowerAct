package com.android.internal.os;

import android.annotation.TargetApi;
import android.os.Build;

@SuppressWarnings("unused")
public final class Zygote {

    /**
     * Executes "/system/bin/sh -c &lt;command&gt;" using the exec() system call.
     * This method throws a runtime exception if exec() failed, otherwise, this
     * method never returns.
     *
     * @param command The shell command to execute.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void execShell(String command) {
    }
}
