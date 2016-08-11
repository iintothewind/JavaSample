package sample.cglib.enhancer;

public class SampleClass {

    public String test(String input) {
        return "Hello world!";
    }

    public void testBlock() {
    }

    @Block(before = 3000, after = 4000)
    public void annotated() {

    }
}