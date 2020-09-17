package github.ryuunoakaihitomi.poweract.internal.util;

import androidx.annotation.Nullable;

import github.ryuunoakaihitomi.poweract.Callback;

public final class CallbackHelper implements Callback {

    private final Callback mCallback;

    private CallbackHelper(Callback callback) {
        mCallback = callback;
    }

    public static CallbackHelper of(@Nullable Callback callback) {
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
