package sample.pool;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

@Ignore
public class PoolTest {

  @Test
  public void testPool() {
    Pool<String> pool = LoadingPool.withSupplier(() -> {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return String.valueOf(System.currentTimeMillis());
    });
    for (int i = 0; i < 99; i++) {
      System.out.println(String.format("%s borrowed: %s", i, pool.borrow()));
    }
  }
}
