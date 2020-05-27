package github.ryuunoakaihitomi.poweract;

/**
 * Callback to notify the result of the operation.
 */
public interface Callback {

    /**
     * Denote the operation completed without any unexpected exception,
     * <b>but does not mean that it is successfully. It cannot be ensure.</b>
     */
    void done();

    /**
     * Denote the operation completed failed.
     */
    void failed();
}
