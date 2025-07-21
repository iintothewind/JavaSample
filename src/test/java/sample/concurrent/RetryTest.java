package sample.concurrent;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RetryTest {
  private ScheduledExecutorService scheduler;
  private RetryExecutor executor;

  @BeforeEach
  public void setUp() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    executor = new AsyncRetryExecutor(scheduler)
      .retryOn(RuntimeException.class)
      .withExponentialBackoff(50, 1)
      .withMaxRetries(3);
  }

  @AfterEach
  public void tearDown() throws InterruptedException {
    scheduler.awaitTermination(3, TimeUnit.SECONDS);
  }

  @Test
  public void testRetry() {
    executor
      .getWithRetry(() -> Integer.parseInt("12a3"))
      .whenComplete((n, e) -> {
        if (n != null) {
          System.out.println(n);
        } else {
          e.printStackTrace();
        }
      });
  }
}
