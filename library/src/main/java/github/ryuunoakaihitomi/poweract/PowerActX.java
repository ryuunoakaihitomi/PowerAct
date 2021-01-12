package github.ryuunoakaihitomi.poweract;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import github.ryuunoakaihitomi.poweract.internal.Initializer;
import github.ryuunoakaihitomi.poweract.internal.pax.PaxConsole;
import github.ryuunoakaihitomi.poweract.internal.pax.PaxInterface;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;

/**
 * This class provides advanced functions of this library.
 * <p>
 * But it requires <b>root</b> privilege. We may have to use {@link PowerAct} instead in most general release environments.
 * <p>
 * <ul><li>
 * It will automatically make all the components that {@link PowerAct} depends on invisible.
 * {@link PowerAct} may not instantly be available after calling it.
 * </li><li>
 * It is recommend to integrate <a href="https://shizuku.rikka.app/">Shizuku</a> to make it transient.
 * </li></ul>
 *
 * @since 1.0.6
 */

@SuppressWarnings("unused")
public class PowerActX {

    private static final String TAG = "PowerActX";

    private PowerActX() {
    }

    static {
        Initializer.notify(TAG);
    }

    /* -------
     LOCK SCREEN
     ------- */

    /**
     * {@link #lockScreen(Callback)}
     */
    public static void lockScreen() {
        lockScreen(null);
    }

    /**
     * Make the device lock immediately.
     *
     * @param callback {@link Callback}
     */
    public static void lockScreen(Callback callback) {
        PaxConsole.getInterface().lockScreen(callback);
    }

    /* -------
    SHUT DOWN
     ------- */

    /**
     * {@link #shutdown(Callback, boolean)}
     */
    public static void shutdown() {
        shutdown(null);
    }

    /**
     * {@link #shutdown(Callback, boolean)}
     *
     * @param callback {@link Callback}
     */
    public static void shutdown(Callback callback) {
        shutdown(callback, false);
    }

    /**
     * {@link #shutdown(Callback, boolean)}
     *
     * @param force f
     * @since 1.0.10
     */
    public static void shutdown(boolean force) {
        shutdown(null, force);
    }

    /**
     * Shut down the device.
     * <p>
     * Can only be forced before 17 because of the lack of the appropriate system API.
     *
     * @param callback {@link Callback}
     * @param force    Be true for low-level shutdown process.
     *                 While it's the fastest way, there may be a <b>safety risk</b>
     *                 that can corrupt data or even damage hardware.
     *                 <b>KEEP FALSE UNTIL YOU KNOW WHAT YOU DO.</b>
     */
    public static void shutdown(Callback callback, boolean force) {
        PaxInterface pi = PaxConsole.getInterface();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DebugLog.w(TAG, "shutdown: Can only be forced before 17. BE CAREFUL!");
            pi.shutdown(callback, true);
        } else {
            pi.shutdown(callback, force);
        }
    }

    /* -------
    REBOOT
     ------- */

    /**
     * {@link #reboot(Callback, boolean)}
     */
    public static void reboot() {
        reboot(null);
    }

    /**
     * {@link #reboot(Callback, boolean)}
     *
     * @param callback {@link Callback}
     */
    public static void reboot(Callback callback) {
        reboot(callback, false);
    }

    /**
     * {@link #reboot(Callback, boolean)}
     *
     * @param force f
     * @since 1.0.10
     */
    public static void reboot(boolean force) {
        reboot(null, force);
    }

    /**
     * Reboot the device.
     *
     * @param callback {@link Callback}
     * @param force    {@link #shutdown(Callback, boolean)}
     */
    public static void reboot(Callback callback, boolean force) {
        PaxConsole.getInterface().reboot(callback, force);
    }

    /* -------
    RECOVERY
     ------- */

    /**
     * {@link #recovery(Callback, boolean)}
     */
    public static void recovery() {
        recovery(null);
    }

    /**
     * {@link #recovery(Callback, boolean)}
     *
     * @param callback {@link Callback}
     */
    public static void recovery(Callback callback) {
        recovery(callback, false);
    }

    /**
     * {@link #recovery(Callback, boolean)}
     *
     * @param force f
     * @since 1.0.10
     */
    public static void recovery(boolean force) {
        recovery(null, force);
    }

    /**
     * Reboot the device to recovery system.
     *
     * @param callback {@link Callback}
     * @param force    {@link #shutdown(Callback, boolean)}
     * @see android.os.RecoverySystem
     */
    public static void recovery(Callback callback, boolean force) {
        PaxConsole.getInterface().recovery(callback, force);
    }

    /* -------
    BOOTLOADER
     ------- */

    /**
     * {@link #bootloader(Callback, boolean)}
     */
    public static void bootloader() {
        bootloader(null);
    }

    /**
     * {@link #bootloader(Callback, boolean)}
     *
     * @param callback {@link Callback}
     */
    public static void bootloader(Callback callback) {
        bootloader(callback, false);
    }

    /**
     * {@link #bootloader(Callback, boolean)}
     *
     * @param force f
     * @since 1.0.10
     */
    public static void bootloader(boolean force) {
        bootloader(null, force);
    }

    /**
     * Reboot the device to bootloader.
     * <p>
     * In this mode, we can control the device with <code>fastboot</code> command-line tool.
     *
     * @param callback {@link Callback}
     * @param force    {@link #safeMode(Callback, boolean)}
     */
    public static void bootloader(Callback callback, boolean force) {
        PaxConsole.getInterface().bootloader(callback, force);
    }

    /* -------
    CUSTOM REBOOT
     ------- */

    /**
     * {@link #customReboot(String, Callback, boolean)}
     *
     * @param arg a
     * @since 1.3.0
     */
    public static void customReboot(String arg) {
        customReboot(arg, false);
    }

    /**
     * {@link #customReboot(String, Callback, boolean)}
     *
     * @param arg      a
     * @param callback {@link Callback}
     * @since 1.3.0
     */
    public static void customReboot(String arg, Callback callback) {
        customReboot(arg, callback, false);
    }

    /**
     * {@link #customReboot(String, Callback, boolean)}
     *
     * @param arg   a
     * @param force f
     * @since 1.3.0
     */
    public static void customReboot(String arg, boolean force) {
        customReboot(arg, null, false);
    }

    /**
     * {@link #reboot(Callback, boolean)} with a additional argument.
     * It is used for some specific environment.
     * <p>
     * For example, devices with Qualcomm SoC have a special boot mode called <b>Emergency Download</b>
     * that allows OEMs to force-flash firmware files.
     * In this situation, we can enter this boot mode with <b>"edl"</b> argument.
     *
     * @param arg      Additional argument for reboot command in shell. <pre>reboot [option]</pre>
     *                 If it is empty, just like {{@link #reboot(Callback, boolean)}}.
     * @param callback {@link Callback}
     * @param force    {@link #shutdown(Callback, boolean)}
     * @since 1.3.0
     */
    public static void customReboot(String arg, Callback callback, boolean force) {
        if (TextUtils.isEmpty(arg)) {
            DebugLog.w(TAG, "customReboot: customReboot() with empty argument!" +
                    " Please consider to use reboot() instead.");
        }
        PaxConsole.getInterface().customReboot(callback, force, arg);
    }

    /* -------
    SAFE MODE
     ------- */

    /**
     * {@link #safeMode(Callback, boolean)}
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void safeMode() {
        safeMode(null);
    }

    /**
     * {@link #safeMode(Callback, boolean)}
     *
     * @param callback {@link Callback}
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void safeMode(Callback callback) {
        safeMode(callback, false);
    }

    /**
     * {@link #safeMode(Callback, boolean)}
     *
     * @param force f
     * @since 1.0.10
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void safeMode(boolean force) {
        safeMode(null, force);
    }

    /**
     * Reboot the device to safe mode.
     * <p>
     * In this mode, all the third party apps will be disabled.
     *
     * @param callback {@link Callback}
     * @param force    {@link #shutdown(Callback, boolean)}
     * @see <a href="https://support.google.com/android/answer/7665064?hl=en">Find problem apps by rebooting to safe mode on Android</a>
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void safeMode(Callback callback, boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            PaxConsole.getInterface().safeMode(callback, force);
        } else {
            // Tried on AVD (Android 4.0.3).
            DebugLog.e(TAG, "safeMode: Does not support before 16.");
            CallbackHelper.of(callback).failed();
        }
    }

    /* -------
    SOFT REBOOT
     ------- */

    /**
     * {@link #softReboot(Callback)}
     */
    public static void softReboot() {
        softReboot(null);
    }

    /**
     * Kill and restart the software system while the hardware parts are still running.
     *
     * @param callback {@link Callback}
     */
    public static void softReboot(Callback callback) {
        PaxConsole.getInterface().softReboot(callback);
    }

    /* -------
    RESTART SYSTEM UI
     ------- */

    /**
     * {@link #restartSystemUi(Callback)}
     *
     * @since 1.2.0
     */
    public static void restartSystemUi() {
        restartSystemUi(null);
    }

    /**
     * Kill the system ui, and then it will restart automatically.
     * <p>
     * It was used to fix the bugs about ui misplaced or not ready in ancient times, but today's system ui rarely have such bugs.
     *
     * @param callback {@link Callback}
     * @since 1.2.0
     */
    public static void restartSystemUi(Callback callback) {
        PaxConsole.getInterface().killSystemUi(callback);
    }
}
