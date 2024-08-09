package sample.sm;


public enum Status {
    CREATED("init", 0),
    TO_DO("todo",1),
    IN_PROGRESS("inProgress",2),
    TEST("test",3),
    CLOSED("closed",4);

    private final String name;
    private final Integer code;

    Status(final String name, final int code) {
        this.name = name;
        this.code = code;
    }
}
