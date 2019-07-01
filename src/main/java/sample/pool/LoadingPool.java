package sample.pool;

import com.google.common.base.Preconditions;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class LoadingPool<T> implements Pool<T> {
  private final int min;
  private final int max;
  private final Supplier<T> supplier;
  private final BlockingQueue<T> queue;
  private final ExecutorService pool;

  public LoadingPool(final int min, final int max, final Supplier<T> supplier) {
    Preconditions.checkArgument(min > 0, "min is required bigger than 0");
    Preconditions.checkArgument(max < 99, "max is required smaller than 99");
    Preconditions.checkArgument(min < max, "max is required smaller than 99");
    Preconditions.checkNotNull(supplier, "supplier is required not null");
    this.min = min;
    this.max = max;
    this.queue = new ArrayBlockingQueue<>(max);
    this.supplier = supplier;
    this.pool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() * 2 + 1);
    init();
  }

  private void init() {
    final int initSize = min - queue.size();
    if (initSize > 0) {
      for (int i = 0; i < initSize; i++) {
        load();
      }
    }
  }

  public void load() {
    if (queue.size() < max) {
      CompletableFuture
        .supplyAsync(supplier, pool)
        .whenComplete((t, throwable) ->
          Option
            .of(t)
            .toTry()
            .onFailure(x -> log.error("error while loading: {}", Option.of(throwable).map(Throwable::getMessage).getOrElse("")))
            .mapTry(queue::add)
            .onFailure(error -> log.warn("error while adding queue: {}", error)));
    }
  }


  @Override
  public T borrow(final long timeout, final TimeUnit timeUnit) {
    load();
    try {
      return queue.poll(timeout, timeUnit);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public T borrow() {
    return borrow(60L, TimeUnit.SECONDS);
  }

  public static <T> LoadingPool<T> withSupplier(Supplier<T> supplier) {
    return new LoadingPool<>(5, 9, supplier);
  }
}
