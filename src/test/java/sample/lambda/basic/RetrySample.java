package sample.lambda.basic;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class RetrySample {
  private RetryExecutor executor;

  @Before
  public void setUp() {
    executor = new AsyncRetryExecutor(Executors.newSingleThreadScheduledExecutor())
      .retryOn(RuntimeException.class)
      .withExponentialBackoff(50, 1)
      .withMaxRetries(5);
  }

  @After
  public void tearDown() throws InterruptedException {
    //        scheduler.awaitTermination(3, TimeUnit.SECONDS);
  }

  @Test
  public void testRetry() {
    final AtomicReference<Integer> result = new AtomicReference<>();
    executor
      .getWithRetry(() -> Integer.parseInt("123"))
      .whenComplete((n, e) -> {
        if (n != null) {
          result.set(n);
        } else {
          e.printStackTrace();
        }
      })
      .join();
    Assertions.assertThat(result.get()).isEqualTo(123);
  }
}
