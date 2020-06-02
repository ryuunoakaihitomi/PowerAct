package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

/**
 * Advanced PowerAct to control the device's power state more directly,
 * and can be invoked everywhere without {@link Activity},
 * but requires <b>root</b> permission.
 */

@SuppressWarnings("unused")
public class PowerActX {

    private static final String TAG = "PowerActX";

    private PowerActX() {
    }

    /**
     * Go to see {@link PowerActX#lockScreen(Callback)}.
     */
    public static void lockScreen() {
        lockScreen(null);
    }

    /**
     * Lock the device without {@link PowerAct#lockScreen(Activity, Callback)}'s restriction.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void lockScreen(Callback callback) {
        PaxConsole.getInterface().lockScreen(callback);
    }

    /**
     * Go to see {@link PowerActX#shutdown(Callback, boolean)}.
     */
    public static void shutdown() {
        shutdown(null);
    }

    /**
     * Go to see {@link PowerActX#shutdown(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void shutdown(Callback callback) {
        shutdown(null, false);
    }

    /**
     * As its name.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    Be true for low-level and instant shutdown process.
     *                 While it's faster than the normal way, there may be a <b>safety risk</b>.
     *                 <b>KEEP FALSE UNTIL YOU KNOW WHAT YOU DO.</b>
     * @see <a href="https://en.wikipedia.org/wiki/Shutdown_(computing)">Shutdown (computing)</a>
     */
    public static void shutdown(Callback callback, boolean force) {
        PaxConsole.getInterface().shutdown(callback, force);
    }

    /**
     * Go to see {@link PowerActX#reboot(Callback, boolean)}.
     */
    public static void reboot() {
        reboot(null);
    }

    /**
     * Go to see {@link PowerActX#reboot(Callback, boolean)}
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void reboot(Callback callback) {
        reboot(null, false);
    }

    /**
     * Reboot the device.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link PowerActX#shutdown(Callback, boolean)}'s <code>force</code>.
     */
    public static void reboot(Callback callback, boolean force) {
        PaxConsole.getInterface().reboot(callback, force);
    }

    /**
     * Go to see {@link PowerActX#recovery(Callback, boolean)}.
     */
    public static void recovery() {
        recovery(null);
    }

    /**
     * Go to see {@link PowerActX#recovery(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void recovery(Callback callback) {
        recovery(callback, false);
    }

    /**
     * Reboot the device to recovery.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link PowerActX#shutdown(Callback, boolean)}'s <code>force</code>.
     */
    public static void recovery(Callback callback, boolean force) {
        PaxConsole.getInterface().recovery(callback, force);
    }

    /**
     * Go to see {@link PowerActX#recovery(Callback, boolean)}.
     */
    public static void bootloader() {
        bootloader(null);
    }

    /**
     * Go to see {@link PowerActX#recovery(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void bootloader(Callback callback) {
        bootloader(callback, false);
    }


    /**
     * Reboot the device to bootloader.
     * In this mode, we can control the device with <code>fastboot</code> command-line tool.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link PowerActX#shutdown(Callback, boolean)}'s <code>force</code>.
     */
    public static void bootloader(Callback callback, boolean force) {
        PaxConsole.getInterface().bootloader(callback, force);
    }

    /**
     * Go to see {@link PowerActX#safeMode(Callback, boolean)}.
     */
    public static void safeMode() {
        safeMode(null);
    }

    /**
     * Go to see {@link PowerActX#safeMode(Callback, boolean)}.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void safeMode(Callback callback) {
        safeMode(callback, false);
    }

    /**
     * Reboot the device to safe mode.
     * In this mode, all the third-part apps will be disabled.
     * Useless before 16.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     * @param force    As {@link PowerActX#shutdown(Callback, boolean)}'s <code>force</code>.
     * @see <a href="https://support.google.com/android/answer/7665064?hl=en">Find problem apps by rebooting to safe mode on Android</a>
     */
    public static void safeMode(Callback callback, boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            PaxConsole.getInterface().safeMode(callback, force);
        } else {
            // Tried on AVD (Android 4.0.3).
            Log.e(TAG, "safeMode: Does not support before 16.");
            callback.failed();
        }
    }

    /**
     * Go to see {@link PowerActX#softReboot(Callback)}.
     */
    public static void softReboot() {
        softReboot(null);
    }

    /**
     * Kill and restart the software system while the hardware parts are still running.
     *
     * @param callback As {@link PowerAct#lockScreen(Activity, Callback)}'s <code>callback</code>.
     */
    public static void softReboot(Callback callback) {
        PaxConsole.getInterface().softReboot(callback);
    }
}
