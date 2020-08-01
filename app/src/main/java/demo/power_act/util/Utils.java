package demo.power_act.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private Utils() {
    }

    public static String timestamp2String(String format, long timestamp) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date(timestamp));
    }
}
