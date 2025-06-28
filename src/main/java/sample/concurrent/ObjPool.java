package sample.concurrent;

import io.vavr.CheckedFunction1;
import io.vavr.Tuple;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ObjPool<T> {
    public final static Duration defaultTimeout = Duration.ofSeconds(15L);
    private final List<T> resources;
    private final BlockingQueue<T> availableObjs;

    public ObjPool(final List<T> resources) {
        if (Objects.nonNull(resources) && !resources.isEmpty()) {
            this.resources = resources;
            this.availableObjs = new LinkedBlockingQueue<>(resources);
        } else {
            throw new IllegalArgumentException("invalid resources");
        }
    }

    public T borrow(final Duration timeout) throws InterruptedException, TimeoutException {
        if (Objects.nonNull(timeout)) {
            T t = availableObjs.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (Objects.isNull(t)) {
                throw new TimeoutException("timeout while trying to borrow obj");
            }

            return t;
        }
        throw new IllegalArgumentException("timeout is required");
    }

    public T borrow() {
        return Try.of(() -> borrow(defaultTimeout))
                .onFailure(t -> log.warn("failed to borrow obj", t))
                .getOrNull();
    }

    public void release(T t) {
        if (!resources.contains(t)) {
            throw new IllegalArgumentException("this object does not belong to the pool");
        }
        availableObjs.offer(t);
    }

    public <R> R consume(final Duration timeout, final CheckedFunction1<T, R> function) {
        final R r = Option.of(function)
                .filter(Objects::nonNull)
                .toTry()
                .mapTry(f -> Tuple.of(borrow(timeout), f))
                .flatMapTry(t -> Try.of(() -> t._2.apply(t._1)).andFinallyTry(() -> release(t._1)))
                .onFailure(t -> log.error("failed to consume obj", t))
                .getOrNull();

        return r;
    }

    public <R> R consume(final CheckedFunction1<T, R> function) {
        final R r = Option.of(function)
                .filter(Objects::nonNull)
                .toTry()
                .mapTry(f -> Tuple.of(borrow(defaultTimeout), f))
                .flatMapTry(t -> Try.of(() -> t._2.apply(t._1)).andFinallyTry(() -> release(t._1)))
                .onFailure(t -> log.error("failed to consume obj", t))
                .getOrNull();

        return r;
    }
}
