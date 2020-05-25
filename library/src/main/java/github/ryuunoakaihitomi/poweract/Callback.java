package github.ryuunoakaihitomi.poweract;

/**
 * Callback to notify the result of the operation.
 */
public interface Callback {

    /**
     * Denote the operation completed without any unexpected exception,
     * <b>but does not mean that it is successfully.</b>
     * <p>
     * For example, if we try to request {@link android.accessibilityservice.AccessibilityService},
     * Whether we grant or deny the permission, it will not be called.
     * But it will be called when we use the permission to do sth.
     */
    void done();

    /**
     * Denote the operation completed failed.
     */
    void failed();
}
