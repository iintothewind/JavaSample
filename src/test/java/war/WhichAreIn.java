package war;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class WhichAreIn {
  public static String[] inArray(String[] array1, String[] array2) {
    return Arrays.stream(array1).filter(a -> Arrays.stream(array2).anyMatch(b -> b.contains(a))).toArray(String[]::new);

  }

  @Test
  public void test1() {
    String a[] = new String[]{"arp", "live", "strong"};
    String b[] = new String[]{"lively", "alive", "harp", "sharp", "armstrong"};
    String r[] = new String[]{"arp", "live", "strong"};
    assertArrayEquals(r, WhichAreIn.inArray(a, b));
  }
}

