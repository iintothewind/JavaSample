package sample.pool;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class PoolTest {

  @Test
  public void testPool() {
    Pool<String> pool = LoadingPool
      .withSupplier(() -> {
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return String.valueOf(System.currentTimeMillis());
      })
      .withMinSize(5)
      .withMaxSize(9)
      .build();
    for (int i = 0; i < 99; i++) {
      System.out.println(String.format("%s borrowed: %s", i, pool.borrow()));
    }
  }

}
