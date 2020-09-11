package github.ryuunoakaihitomi.poweract;

import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface PaxExecApi {

    @Type
    String value();

    @StringDef({
            PaxExecutor.TOKEN_LOCK_SCREEN,
            PaxExecutor.TOKEN_REBOOT,
            PaxExecutor.TOKEN_SHUTDOWN,
            PaxExecutor.TOKEN_RECOVERY,
            PaxExecutor.TOKEN_BOOTLOADER,
            PaxExecutor.TOKEN_SAFE_MODE,
            PaxExecutor.TOKEN_SOFT_REBOOT,
            PaxExecutor.TOKEN_KILL_SYSTEM_UI
    })
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
    }
}
