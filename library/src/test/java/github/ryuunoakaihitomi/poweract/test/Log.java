package github.ryuunoakaihitomi.poweract.test;

interface Log {

    default void i(String msg) {
        System.out.println(msg);
    }
}
