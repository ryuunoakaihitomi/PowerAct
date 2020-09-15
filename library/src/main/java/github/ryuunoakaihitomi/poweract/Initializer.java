package github.ryuunoakaihitomi.poweract;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogPrinter;

import java.lang.reflect.Field;
import java.util.Arrays;

final class Initializer {

    private static final String TAG = "Initializer";

    static {
        // For copyright protection.
        new LogPrinter(Log.WARN, "PowerAct").println("http://www.apache.org/licenses/LICENSE-2.0.html");
        /* Debug special models' environment. */
        printAllPublicConstants("build:", Build.class);
        printAllPublicConstants("version:", Build.VERSION.class);
    }

    private Initializer() {
    }

    public static void notify(String entry) {
        DebugLog.d(TAG, "notify: Calling " + entry);
    }

    private static void printAllPublicConstants(String namePrefix, Class<?> clazz) {
        for (Field f : clazz.getFields()) {
            try {
                final String name = f.getName();
                final Object value = f.get(null);
                if (value != null) {
                    String printableValue = null;
                    if (value.getClass().isArray()) {
                        Object[] arrayValue = (Object[]) value;
                        if (arrayValue.length > 0) {
                            printableValue = Arrays.toString((Object[]) value);
                        }
                    } else {
                        printableValue = value.toString();
                    }
                    final String tag = namePrefix == null ? name : namePrefix + name;
                    if (!TextUtils.isEmpty(printableValue)) {
                        DebugLog.i(TAG, "printAllPublicConstants: " +
                                (f.isAnnotationPresent(Deprecated.class) ? "Deprecated " : "") +
                                Arrays.asList(tag, printableValue));
                    } else {
                        DebugLog.i(TAG, "printAllPublicConstants: Empty " + tag);
                    }
                }
            } catch (IllegalAccessException e) {
                DebugLog.v(TAG, "printAllPublicConstants: ", e);
            }
        }
    }
}
