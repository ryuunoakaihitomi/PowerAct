package android.os;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Gives access to the system properties store.  The system properties
 * store contains a list of string key-value pairs.
 *
 * <p>Use this class only for the system properties that are local. e.g., within
 * an app, a partition, or a module. For system properties used across the
 * boundaries, formally define them in <code>*.sysprop</code> files and use the
 * auto-generated methods. For more information, see <a href=
 * "https://source.android.com/devices/architecture/sysprops-apis">Implementing
 * System Properties as APIs</a>.</p>
 */
@SuppressWarnings("unused")
public class SystemProperties {

    /**
     * Set the value for the given {@code key} to {@code val}.
     *
     * @throws IllegalArgumentException if the {@code val} exceeds 91 characters
     * @throws RuntimeException         if the property cannot be set, for example, if it was blocked by
     *                                  SELinux. libc will log the underlying reason.
     */
    public static void set(@NonNull String key, @Nullable String val) {
    }
}
