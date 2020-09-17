package github.ryuunoakaihitomi.poweract.internal.pa;

import androidx.annotation.IntDef;
import androidx.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class PaConstants {

    @Keep
    public static final int
            ACTION_LOCK_SCREEN = 1,
            ACTION_POWER_DIALOG = 2,
            ACTION_REBOOT = 3;

    private PaConstants() {
    }

    @IntDef({
            ACTION_LOCK_SCREEN,
            ACTION_POWER_DIALOG,
            ACTION_REBOOT
    })
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }
}
