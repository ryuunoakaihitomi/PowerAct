package github.ryuunoakaihitomi.poweract.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class BorderLogger implements TestRule {

    private static final
    String TAG = "BorderLogger",
            START_DIVIDER = buildDivider('-', 7),
            END_DIVIDER = buildDivider(' ', 4);

    private final static Logger sLogger;

    static {
        sLogger = Logger.getLogger(TAG);
        sLogger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                final String time = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.getMillis()), ZoneId.systemDefault()).toString();
                return Arrays.asList(time, record.getLevel()) + record.getMessage() + System.lineSeparator();
            }
        });
        sLogger.addHandler(handler);
    }

    private static String buildDivider(final char dividerChar, final int dividerLength) {
        StringBuilder divider = new StringBuilder();
        for (int i = 0; i < dividerLength; i++) divider.append(dividerChar);
        return divider.toString();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        final String who = description.getDisplayName();
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                sLogger.info(START_DIVIDER + who);
                base.evaluate();
                sLogger.info(END_DIVIDER + who);
            }
        };
    }
}
