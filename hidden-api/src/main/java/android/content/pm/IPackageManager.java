package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.NonNull;

/**
 * See {@link PackageManager} for documentation on most of the APIs
 * here.
 */
public interface IPackageManager extends IInterface {

    // @UnsupportedAppUsage
    String getNameForUid(int uid);

    abstract class Stub extends Binder implements IPackageManager {

        @SuppressWarnings({"ConstantConditions", "unused"})
        public static @NonNull
        IPackageManager asInterface(IBinder var0) {
            return null;
        }
    }
}
