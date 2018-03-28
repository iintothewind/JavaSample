package sample.concurrent;


import com.google.common.base.Throwables;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * Sample code for: http://ifeve.com/completablefuture/
 * CompletableFuture 不能被中断
 */
class InterruptableTask implements Callable<String> {
  private final Logger log = LogManager.getLogger(this.getClass().getName());
  private final CountDownLatch started = new CountDownLatch(1);
  private final CountDownLatch interrupted = new CountDownLatch(1);

  @Override
  public String call() {
    log.info("in call, try to sleep");
    started.countDown();
    // @formatter:off
        // InterruptedException belongs to FatalException, thus it will be thrown but never hold in Failure
        Try.run(() -> {try{TimeUnit.SECONDS.sleep(10);}catch(InterruptedException e){Throwables.propagate(e);}})
           .onFailure(e -> {log.info("sleep interrupted");interrupted.countDown();});
        // @formatter:on
    return "Done";
  }

  public void blockUntilStarted() throws InterruptedException {
    started.await();
  }

  public boolean blockUntilInterrupted() throws InterruptedException {
    return interrupted.await(100, TimeUnit.MILLISECONDS);
  }
}

public class FutureCancelSample {

  private ExecutorService pool = null;

  @Before
  public void setUp() {

  }

  @After
  public void tearDown() {
    Optional.ofNullable(pool).ifPresent((p) -> Try.of(() -> p.awaitTermination(500, TimeUnit.MILLISECONDS)));
  }

  @Test(expected = CancellationException.class)
  public void testCancelFutureTask() throws ExecutionException, InterruptedException {
    pool = Executors.newCachedThreadPool();
    InterruptableTask task = new InterruptableTask();
    Future<String> future = pool.submit(task);
    task.blockUntilStarted();
    future.cancel(true); // set true to interrupt the running thread, no exception is thrown from here
    future.get(); // throws CancellationException
  }

  @Test(expected = CancellationException.class)
  public void testCancelCompletableFutureTask() throws InterruptedException, ExecutionException {
    pool = Executors.newWorkStealingPool();
    InterruptableTask task = new InterruptableTask();
    Future<String> future = CompletableFuture.supplyAsync(task::call, pool);
    task.blockUntilStarted();
    future.cancel(true); // CancellationException is thrown, not like the above example
    future.get();
  }

  @Test
  public void testInterruptFutureTask() throws InterruptedException, ExecutionException {
    pool = Executors.newCachedThreadPool();
    InterruptableTask task = new InterruptableTask();
    Future<String> future = pool.submit(task);
    task.blockUntilStarted();
    future.cancel(true); // set true to interrupt the running thread, no exception is thrown from here
    Assertions.assertThat(task.blockUntilInterrupted()).isTrue(); // thread.sleep() is interrupted, hence returns true
  }

  @Test
  public void testInterruptCompletableFutureTask() throws InterruptedException {
    pool = Executors.newWorkStealingPool();
    InterruptableTask task = new InterruptableTask();
    Future<String> future = CompletableFuture.supplyAsync(task::call, pool);
    task.blockUntilStarted();
    future.cancel(true); // CancellationException is thrown, not like the above example
    Assertions.assertThat(task.blockUntilInterrupted()).isFalse(); // thread.sleep() is never interrupted
  }
}
