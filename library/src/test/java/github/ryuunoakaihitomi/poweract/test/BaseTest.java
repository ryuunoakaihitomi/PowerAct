package github.ryuunoakaihitomi.poweract.test;

import org.junit.Rule;

public class BaseTest implements Log {

    @Rule
    public BorderLogger borderLogger = new BorderLogger();
}
