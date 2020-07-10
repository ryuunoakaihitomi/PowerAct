package github.ryuunoakaihitomi.poweract;

import androidx.annotation.Nullable;

class CallbackHelper implements Callback {

    private final Callback mCallback;

    private CallbackHelper(Callback callback) {
        mCallback = callback;
    }

    static CallbackHelper of(@Nullable Callback callback) {
        return new CallbackHelper(callback);
    }

    @Override
    public void done() {
        if (nonNull()) mCallback.done();
    }

    @Override
    public void failed() {
        if (nonNull()) mCallback.failed();
    }

    private boolean nonNull() {
        return mCallback != null;
    }
}
