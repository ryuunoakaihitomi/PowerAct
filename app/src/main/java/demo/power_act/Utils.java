package demo.power_act;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Utils {

    private Utils() {
    }

    public static String timestamp2String(String format, long timestamp) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date(timestamp));
    }
}
