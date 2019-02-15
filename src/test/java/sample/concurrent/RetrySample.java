package sample.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;


public class RetrySample {
    private ScheduledExecutorService scheduler;
    private RetryExecutor executor;

    @Before
    public void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        executor = new AsyncRetryExecutor(scheduler)
            .retryOn(RuntimeException.class)
            .withExponentialBackoff(50, 1)
            .withMaxRetries(3);
    }

    @After
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
