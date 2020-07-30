package sample.concurrent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

public interface TimeoutSupportFuture {

  static <T> CompletableFuture<T> within(CompletableFuture<T> completableFuture, Duration duration) {
    Objects.requireNonNull(completableFuture, "completableFuture is required");
    Objects.requireNonNull(duration, "duration is required");
    final CompletableFuture<T> promiseFailed = new CompletableFuture<>();
    Delayer.delay(new Timeout(promiseFailed), duration.toMillis(), TimeUnit.MILLISECONDS);
    return completableFuture.applyToEither(promiseFailed, Function.identity());
  }

  /**
   * Action to completeExceptionally on timeout
   * copy from `java.util.concurrent.CompletableFuture.Timeout` in java 9 JDK
   */
  final class Timeout implements Runnable {
    final CompletableFuture<?> future;

    Timeout(CompletableFuture<?> f) {
      this.future = f;
    }

    @Override
    public void run() {
      if (future != null && !future.isDone()) {
        future.completeExceptionally(new TimeoutException());
      }
    }
  }

  /**
   * Singleton delay scheduler, used only for starting and cancelling tasks.
   * copy from `java.util.concurrent.CompletableFuture.Delayer` in java 9 JDK
   */
  final class Delayer {
    static final ScheduledThreadPoolExecutor delayer;

    static {
      (delayer = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory())).setRemoveOnCancelPolicy(true);
    }

    static ScheduledFuture<?> delay(Runnable command, long delay, TimeUnit unit) {
      return delayer.schedule(command, delay, unit);
    }

    static final class DaemonThreadFactory implements ThreadFactory {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.setName("CompletableFutureDelayScheduler");
        return t;
      }
    }
  }
}
