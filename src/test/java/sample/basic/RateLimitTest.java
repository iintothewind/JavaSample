package sample.basic;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.vavr.Tuple;
import io.vavr.collection.Stream;
import io.vavr.control.Try;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class RateLimitTest {

    @Test
    public void testRl01() {
        final RateLimiterConfig config = RateLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(0L)) // no wait time, fail directly when operation is limited
            .limitRefreshPeriod(Duration.ofSeconds(1L)) // rateLimit on every second
            .limitForPeriod(5) // limit 5 operations per rateLimit period
            .drainPermissionsOnResult(t -> true)
            .writableStackTraceEnabled(Boolean.FALSE)
            .build();
        final RateLimiter rateLimiter = RateLimiter.of("rateLimiter", config);
        final CheckedSupplier<String> timeSupplier = RateLimiter.decorateCheckedSupplier(rateLimiter, () -> String.format("time: %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        Stream.from(1).takeWhile(i -> i < 9999)
            .toJavaList()
            .parallelStream()
            .map(i -> Tuple.of(i, Try.of(timeSupplier::get)
//                .onFailure(t -> log.warn("failed to get time,", t))
                .getOrNull()))
            .filter(t -> Objects.nonNull(t._2))
            .forEach(t -> log.info("i: {}, time: {}", t._1, t._2));
    }

}
