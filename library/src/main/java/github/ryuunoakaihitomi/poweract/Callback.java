package github.ryuunoakaihitomi.poweract;

/**
 * Callback to notify the result of the operation.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface Callback {

    /**
     * Denote the operation completed without any <b>unexpected exception or permission denial</b>.
     * <p>
     * But does not mean that it is successfully, it's not sure.
     */
    default void done() {
    }

    /**
     * Denote the operation completed failed.
     * We'd better tell user it.
     *
     * @see #done()
     */
    void failed();
}
