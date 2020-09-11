package github.ryuunoakaihitomi.poweract;

interface PaxInterface {

    @PaxExecApi(PaxExecutor.TOKEN_LOCK_SCREEN)
    void lockScreen(Callback callback);

    @PaxExecApi(PaxExecutor.TOKEN_SHUTDOWN)
    void shutdown(Callback callback, boolean force);

    @PaxExecApi(PaxExecutor.TOKEN_REBOOT)
    void reboot(Callback callback, boolean force);

    @PaxExecApi(PaxExecutor.TOKEN_RECOVERY)
    void recovery(Callback callback, boolean force);

    @PaxExecApi(PaxExecutor.TOKEN_BOOTLOADER)
    void bootloader(Callback callback, boolean force);

    @PaxExecApi(PaxExecutor.TOKEN_SAFE_MODE)
    void safeMode(Callback callback, boolean force);

    @PaxExecApi(PaxExecutor.TOKEN_SOFT_REBOOT)
    void softReboot(Callback callback);

    @PaxExecApi(PaxExecutor.TOKEN_KILL_SYSTEM_UI)
    void killSystemUi(Callback callback);
}
