package sample.pool;

import com.google.common.base.Preconditions;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class LoadingPool<T> implements Pool<T> {
  public final static int DEFAULT_POOL_MIN_SIZE = 1;
  public final static int DEFAULT_POOL_MAX_SIZE = 9;
  public final static int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors() * 2 + 1;
  public final static Duration DEFAULT_BORROW_TIMEOUT = Duration.ofSeconds(60L);
  private final int minSize;
  private final int maxSize;
  private final Duration defaultBorrowTimeout;
  private final Supplier<T> supplier;
  private final BlockingQueue<T> queue;
  private final ExecutorService pool;

  private LoadingPool(final int minSize, final int maxSize, final int parallelism, final Duration defaultBorrowTimeout, final Supplier<T> supplier) {
    Preconditions.checkArgument(minSize > 0, "minSize is required bigger than 0");
    Preconditions.checkArgument(maxSize < 99, "maxSize is required smaller than 99");
    Preconditions.checkArgument(minSize < maxSize, "maxSize is required smaller than 99");
    Preconditions.checkArgument(parallelism > 0, "parallelism is required bigger than 0");
    Preconditions.checkNotNull(defaultBorrowTimeout, "defaultBorrowTimeout is required not null");
    Preconditions.checkNotNull(supplier, "supplier is required not null");
    this.minSize = minSize;
    this.maxSize = maxSize;
    this.queue = new ArrayBlockingQueue<>(maxSize);
    this.supplier = supplier;
    this.pool = Executors.newWorkStealingPool(parallelism);
    this.defaultBorrowTimeout = defaultBorrowTimeout;
    provision(minSize);
  }

  private void load() {
    if (queue.size() < maxSize) {
      CompletableFuture
        .supplyAsync(supplier, pool)
        .whenComplete((t, throwable) ->
          Option
            .of(t)
            .toTry()
            .onFailure(x -> log.error("error while loading: {}", Option.of(throwable).map(Throwable::getMessage).getOrElse("")))
            .mapTry(queue::add)
            .onFailure(exception -> log.warn("error while adding queue: {}", exception.getMessage())));
    }
  }

  private void provision(final int number) {
    Stream.from(0).takeWhile(i -> i < number).forEach(i -> load());
  }

  @Override
  public T borrow(final long timeout, final TimeUnit timeUnit) {
    try {
      return queue.poll(timeout, timeUnit);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      if (queue.size() < minSize) {
        provision(minSize - queue.size());
      }
    }
  }

  @Override
  public T borrow() {
    return borrow(defaultBorrowTimeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  public static <T> Builder<T> withSupplier(final Supplier<T> supplier) {
    return new Builder<T>(DEFAULT_POOL_MIN_SIZE, DEFAULT_POOL_MAX_SIZE, DEFAULT_PARALLELISM, DEFAULT_BORROW_TIMEOUT, supplier);
  }

  public static final class Builder<T> {
    private final int minSize;
    private final int maxSize;
    private final int parallelism;
    private final Duration defaultBorrowTimeout;
    private final Supplier<T> supplier;

    private Builder(final int minSize, final int maxSize, final int parallelism, final Duration defaultBorrowTimeout, final Supplier<T> supplier) {
      this.minSize = minSize;
      this.maxSize = maxSize;
      this.parallelism = parallelism;
      this.defaultBorrowTimeout = defaultBorrowTimeout;
      this.supplier = supplier;
    }

    public Builder<T> withMinSize(final int minSize) {
      return new Builder<>(minSize, maxSize, parallelism, defaultBorrowTimeout, supplier);
    }

    public Builder<T> withMaxSize(final int maxSize) {
      return new Builder<>(minSize, maxSize, parallelism, defaultBorrowTimeout, supplier);
    }

    public Builder<T> withParallelism(final int parallelism) {
      return new Builder<>(minSize, maxSize, parallelism, defaultBorrowTimeout, supplier);
    }

    public Builder<T> withDefaultBorrowTimeout(final Duration defaultBorrowTimeout) {
      return new Builder<>(minSize, maxSize, parallelism, defaultBorrowTimeout, supplier);
    }

    public Builder<T> withSupplier(final Supplier<T> supplier) {
      return new Builder<>(minSize, maxSize, parallelism, defaultBorrowTimeout, supplier);
    }

    public LoadingPool<T> build() {
      return new LoadingPool<>(minSize, maxSize, parallelism, defaultBorrowTimeout, supplier);
    }
  }

}

