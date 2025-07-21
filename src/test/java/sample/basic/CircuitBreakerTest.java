package sample.basic;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

@Slf4j
public class CircuitBreakerTest {
    private CircuitBreakerConfig circuitBreakerConfig;

    @BeforeEach
    public void setUp() {
        circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(10)
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .recordExceptions(IllegalStateException.class)
                .build();
    }

    public static int processNum(int i) {
        if (i % 2==0) {
            return i / 2;
        }
        throw new IllegalStateException(String.format("odd number: %s", i));
    }

    private boolean flag = true;


    @Test
    public void testCircuitBreaker01() {
        final CircuitBreaker circuitBreaker = CircuitBreaker.of("test01", circuitBreakerConfig);

        IntStream.range(1, 99).forEach(i -> Try
                .ofSupplier(circuitBreaker.decorateSupplier(() -> processNum(i)))
                .onFailure(t -> log.warn("failed, i: {}", i, t))
                .onFailure(CallNotPermittedException.class, t -> {
                    if (i>=20) {
                        circuitBreaker.reset();
                    }
                })
                .forEach(n -> log.info("processed Num: {}", n)));
    }

}
