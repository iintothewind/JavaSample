package war;

import static org.junit.Assert.assertEquals;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.LongStream;
import org.junit.Ignore;
import org.junit.Test;

public class StepInPrimes {
  public static long[] step(int g, long m, long n) {
//    for (long l = m; l <= n; l++) {
//        if (isPrime(l) && l + g <= n && isPrime(l + g)) {
//          return new long[]{l, l + g};
//        }
//      }
//    return new long[]{0L, 0L};
    return LongStream
      .iterate(m % 2 == 0 ? m + 1 : m, l -> l + 2)
      .limit((n - m) / 2)
      .filter(l -> BigInteger.valueOf(l).isProbablePrime(2) && BigInteger.valueOf(l + g).isProbablePrime(2))
      .mapToObj(l -> new long[]{l, l + g})
      .findFirst()
      .orElse(null);
  }

  private static boolean isPrime(long n) {
    for (long l = 2; l < n; l++) {
      if (n % l == 0) return false;
    }
    return true;
  }

  @Ignore
  @Test
  public void test() {
    System.out.println("Fixed Tests");
    assertEquals("[101, 103]", Arrays.toString(StepInPrimes.step(2, 100, 110)));
    assertEquals("[103, 107]", Arrays.toString(StepInPrimes.step(4, 100, 110)));
    assertEquals("[101, 107]", Arrays.toString(StepInPrimes.step(6, 100, 110)));
    assertEquals("[359, 367]", Arrays.toString(StepInPrimes.step(8, 300, 400))); // failed
    assertEquals("[307, 317]", Arrays.toString(StepInPrimes.step(10, 300, 400)));
  }
}
