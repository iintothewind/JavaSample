package sample.lambda.basic;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;


public class RetrySample {
    private ScheduledExecutorService scheduledExecutorService;
    private RetryExecutor executor;

    @Before
    public void setUp() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        executor = new AsyncRetryExecutor(scheduledExecutorService)
            .retryOn(RuntimeException.class)
            .withExponentialBackoff(50, 1)
            .withMaxRetries(5);
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
}
