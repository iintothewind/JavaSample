package sample.basic;

import static io.vavr.API.$;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.API;
import io.vavr.Predicates;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class RetrySample {

    private ScheduledExecutorService scheduledExecutorService;
    private RetryExecutor executor;
    private RetryConfig retryConfig;
    private RetryConfig intRetryConfig;

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

        intRetryConfig = RetryConfig.<Integer>custom()
            .maxAttempts(10)
            .waitDuration(Duration.ofMillis(100))
            .retryOnResult(num -> Optional.ofNullable(num).filter(i -> i <= 0).isPresent())
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
        final CheckedFunction<String, Integer> retryParseInt = Retry.<String, Integer>decorateCheckedFunction(Retry.of("test", retryConfig), Integer::parseInt);
        final Integer result = Try.of(() -> retryParseInt.apply("999a")).getOrElse(-1);
        Assertions.assertThat(result).isEqualTo(-1);
    }

    @Test
    public void testIntRetryConfig() {
        final CheckedSupplier<Integer> supplier = Retry.decorateCheckedSupplier(Retry.of("intRtry", intRetryConfig), () -> {
            log.info("try to get a number");
            return 0 - ThreadLocalRandom.current().nextInt(10);
        });
        final int result = Try.of(supplier::get)
            .onSuccess(n -> log.info("the final num: {}", n))
            .getOrElseThrow(throwable -> new IllegalStateException(throwable.getMessage()));

        System.out.println(result);

    }
}

