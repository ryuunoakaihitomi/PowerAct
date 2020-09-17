package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import github.ryuunoakaihitomi.poweract.internal.Initializer;
import github.ryuunoakaihitomi.poweract.internal.pax.PaxConsole;
import github.ryuunoakaihitomi.poweract.internal.pax.PaxInterface;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;

/**
 * Advanced PowerAct to control the device's power state more directly,
 * and can be invoked everywhere without {@link Activity},
 * but requires <b>root</b> permission.
 * <p>
 * Note: It will disable all the components that {@link PowerAct} depends on.
 * Once disabled, they cannot be enabled instantly.
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

    /**
     * Go to see {@link #lockScreen(Callback)}.
     */
    public static void lockScreen() {
        lockScreen(null);
    }

    /**
     * Lock the device without some {@link PowerAct#lockScreen(Activity, Callback)}'s restriction:
     * the user can unlock by biometric sensors before 28.
     * Since 28, We don't need to keep the {@link android.accessibilityservice.AccessibilityService} alive in the background.
     * <p>
     * Although it's very <b>slow</b> (In some environments, the user may need to wait a second or longer) in most cases,
     * but we can integrate with <a href="https://shizuku.rikka.app/">Shizuku</a>
     * to make it as fast as {@link PowerAct#lockScreen(Activity, Callback)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void lockScreen(Callback callback) {
        PaxConsole.getInterface().lockScreen(callback);
    }

    /**
     * Go to see {@link #shutdown(Callback, boolean)}.
     */
    public static void shutdown() {
        shutdown(null);
    }

    /**
     * Go to see {@link #shutdown(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void shutdown(Callback callback) {
        shutdown(callback, false);
    }

    /**
     * Go to see {@link #shutdown(Callback, boolean)}.
     *
     * @param force As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     * @since 1.0.10
     */
    public static void shutdown(boolean force) {
        shutdown(null, force);
    }

    /**
     * As its name.
     * <b>Can only be forced before 17.</b>
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    Be true for low-level and instant shutdown process.
     *                 While it's faster than the normal way, there may be a <b>safety risk</b>.
     *                 <b>KEEP FALSE UNTIL YOU KNOW WHAT YOU DO.</b>
     * @see <a href="https://en.wikipedia.org/wiki/Shutdown_(computing)">Shutdown (computing)</a>
     */
    public static void shutdown(Callback callback, boolean force) {
        PaxInterface pi = PaxConsole.getInterface();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.w(TAG, "shutdown: Can only be forced before 17. BE CAREFUL!");
            pi.shutdown(callback, true);
        } else {
            pi.shutdown(callback, force);
        }
    }

    /**
     * Go to see {@link #reboot(Callback, boolean)}.
     */
    public static void reboot() {
        reboot(null);
    }

    /**
     * Go to see {@link #reboot(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void reboot(Callback callback) {
        reboot(callback, false);
    }

    /**
     * Go to see {@link #reboot(Callback, boolean)}.
     *
     * @param force As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     * @since 1.0.10
     */
    public static void reboot(boolean force) {
        reboot(null, force);
    }

    /**
     * Reboot the device.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     */
    public static void reboot(Callback callback, boolean force) {
        PaxConsole.getInterface().reboot(callback, force);
    }

    /**
     * Go to see {@link #recovery(Callback, boolean)}.
     */
    public static void recovery() {
        recovery(null);
    }

    /**
     * Go to see {@link #recovery(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void recovery(Callback callback) {
        recovery(callback, false);
    }

    /**
     * Go to see {@link #recovery(Callback, boolean)}.
     *
     * @param force As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     * @since 1.0.10
     */
    public static void recovery(boolean force) {
        recovery(null, force);
    }

    /**
     * Reboot the device to recovery.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     */
    public static void recovery(Callback callback, boolean force) {
        PaxConsole.getInterface().recovery(callback, force);
    }

    /**
     * Go to see {@link #bootloader(Callback, boolean)}.
     */
    public static void bootloader() {
        bootloader(null);
    }

    /**
     * Go to see {@link #bootloader(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void bootloader(Callback callback) {
        bootloader(callback, false);
    }

    /**
     * Go to see {@link #bootloader(Callback, boolean)}.
     *
     * @param force As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     * @since 1.0.10
     */
    public static void bootloader(boolean force) {
        bootloader(null, force);
    }

    /**
     * Reboot the device to bootloader.
     * In this mode, we can control the device with <code>fastboot</code> command-line tool.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     */
    public static void bootloader(Callback callback, boolean force) {
        PaxConsole.getInterface().bootloader(callback, force);
    }

    /**
     * Go to see {@link #safeMode(Callback, boolean)}.
     */
    public static void safeMode() {
        safeMode(null);
    }

    /**
     * Go to see {@link #safeMode(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void safeMode(Callback callback) {
        safeMode(callback, false);
    }

    /**
     * Go to see {@link #safeMode(Callback, boolean)}.
     *
     * @param force As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     * @since 1.0.10
     */
    public static void safeMode(boolean force) {
        safeMode(null, force);
    }

    /**
     * Reboot the device to safe mode.
     * In this mode, all the third-part apps will be disabled.
     * Useless before 16.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link #shutdown(Callback, boolean)}'s <code>force</code>.
     * @see <a href="https://support.google.com/android/answer/7665064?hl=en">Find problem apps by rebooting to safe mode on Android</a>
     */
    public static void safeMode(Callback callback, boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            PaxConsole.getInterface().safeMode(callback, force);
        } else {
            // Tried on AVD (Android 4.0.3).
            Log.e(TAG, "safeMode: Does not support before 16.");
            CallbackHelper.of(callback).failed();
        }
    }

    /**
     * Go to see {@link #softReboot(Callback)}.
     */
    public static void softReboot() {
        softReboot(null);
    }

    /**
     * Kill and restart the software system while the hardware parts are still running.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @see <a href="https://developer.android.com/reference/android/os/PowerManager#isRebootingUserspaceSupported()">Rebooting userspace</a>
     */
    public static void softReboot(Callback callback) {
        PaxConsole.getInterface().softReboot(callback);
    }

    /**
     * Go to see {@link #restartSystemUi(Callback)}.
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
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @since 1.2.0
     */
    public static void restartSystemUi(Callback callback) {
        PaxConsole.getInterface().killSystemUi(callback);
    }
}
