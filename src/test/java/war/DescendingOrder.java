package war;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class DescendingOrder {
  public static int sortDesc(final int num) {
    return Integer.parseInt(String.valueOf(num).chars().mapToObj(c -> String.valueOf((char) c)).sorted(Comparator.<String>comparingInt(Integer::parseInt).reversed()).reduce("", String::concat));
  }

  @Test
  public void test_01() {
    assertEquals(0, DescendingOrder.sortDesc(0));
  }

  @Test
  public void test_02() {
    assertEquals(51, DescendingOrder.sortDesc(15));
  }


  @Test
  public void test_03() {
    assertEquals(987654321, DescendingOrder.sortDesc(123456789));
  }
}
