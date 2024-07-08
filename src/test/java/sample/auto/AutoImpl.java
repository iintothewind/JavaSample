package sample.auto;

public class AutoImpl {

    private String name;

    public void setName(final String name) {
        this.name = name;
    }

    @CustomAuto
    public String getName() {
        return name;
    }
}
