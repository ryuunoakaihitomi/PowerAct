package github.ryuunoakaihitomi.poweract;

/**
 * Callback to notify the result of the operation when {@link PowerAct} or {@link PowerActX} being called.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface Callback {

    /**
     * Denote the operation completed without any <b>{@link Exception} or permission denial</b>.
     */
    default void done() {
    }

    /**
     * Denote the operation completed unsuccessfully.
     *
     * @see #done() to check the possible cause.
     */
    void failed();
}
