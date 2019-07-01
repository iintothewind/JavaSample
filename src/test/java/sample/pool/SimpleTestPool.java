package sample.pool;

import java.util.concurrent.TimeUnit;

public class SimpleTestPool extends LoadingPool<String> {
  public SimpleTestPool() {
    super(5, 9, () -> {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return String.valueOf(System.currentTimeMillis());
    });
  }
}
