package sample.cglib.enhancer;

public class SampleClass {

  public String test(String input) {
    return "Hello world!";
  }

  public void testBlock() {
  }

  @Block(before = 300, after = 400)
  public void annotated() {

  }
}