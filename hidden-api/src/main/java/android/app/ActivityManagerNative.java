package android.app;

/**
 * Deprecated since 26.
 */
public final /*abstract*/ class ActivityManagerNative {

    /**
     * Retrieve the system's default/global activity manager.
     * <p>
     * DEPRECATED, use ActivityManager.getService instead.
     */
    public static IActivityManager getDefault() {
        return null;
    }
}
