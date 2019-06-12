package sample.basic;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.API;
import io.vavr.CheckedFunction1;
import io.vavr.Predicates;
import io.vavr.control.Try;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.vavr.API.$;


public class RetrySample {
  private ScheduledExecutorService scheduledExecutorService;
  private RetryExecutor executor;
  private RetryConfig retryConfig;

  @Before
  public void setUp() {
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    executor = new AsyncRetryExecutor(scheduledExecutorService)
      .retryOn(RuntimeException.class)
      .withExponentialBackoff(50, 1)
      .withMaxRetries(5);

    retryConfig = RetryConfig.custom()
      .maxAttempts(2)
      .waitDuration(Duration.ofMillis(100))
      .retryOnException(throwable -> API.Match(throwable).of(
        API.Case($(Predicates.instanceOf(RuntimeException.class)), true),
        API.Case($(), false)))
      .build();
  }

  @After
  public void tearDown() throws InterruptedException {
    scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
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

  @Test
  public void testRetryConfig() {
    final CheckedFunction1<String, Integer> retryParseInt = Retry.decorateCheckedFunction(Retry.of("test", retryConfig), Integer::parseInt);
    final Integer result = Try.of(() -> retryParseInt.apply("999a")).getOrElse(-1);
    Assertions.assertThat(result).isEqualTo(-1);
  }
}

