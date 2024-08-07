package sample.sm;

public enum Event {
    CREATE("create", 1),
    plan("plan", 2),
    develop("fix", 3),
    test("test", 4),
    close("close", 5);

    Event(final String close, final int code) {
    }
}
