package war;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DigitalRoot {
  public static int digitalRoot(int n) {
//    int m = Optional.of(n)
//      .filter(a -> a > 9)
//      .map(i -> String.valueOf(i).codePoints().mapToObj(c -> String.valueOf((char) c)).mapToInt(Integer::parseInt).sum())
//      .orElse(n);
//    if (m == n) {
//      return n;
//    } else {
//      return digitalRoot(m);
//    }
    return (n != 0 && n % 9 == 0) ? 9 : n % 9;
  }

  @Test
  public void Tests() {
    assertEquals("Nope!", digitalRoot(16), 7);
  }

}
