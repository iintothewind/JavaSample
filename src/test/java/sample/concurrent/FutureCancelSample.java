package sample.concurrent;


import com.google.common.base.Throwables;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sample code for: http://ifeve.com/completablefuture/
 * CompletableFuture 不能被中断
 */
@Slf4j
class InterruptableTask implements Callable<String> {
  private final CountDownLatch started = new CountDownLatch(1);
  private final CountDownLatch interrupted = new CountDownLatch(1);

  @Override
  public String call() {
    log.info("in call, try to sleep");
    started.countDown();
    // InterruptedException belongs to FatalException, thus it will be thrown but never hold in Failure
    Try.run(() -> TimeUnit.SECONDS.sleep(10))
      .onFailure(e -> {
        log.info("sleep interrupted: {}", e.getMessage());
        interrupted.countDown();
      });
    return "Done";
  }

  public void blockUntilStarted() throws InterruptedException {
    started.await();
  }

  public boolean blockUntilInterrupted() throws InterruptedException {
    return interrupted.await(100, TimeUnit.MILLISECONDS);
  }
}

@Slf4j
public class FutureCancelSample {

  private ExecutorService pool = null;

  @Before
  public void setUp() {

  }

  @After
  public void tearDown() {
    Optional.ofNullable(pool).ifPresent((p) -> Try.of(() -> p.awaitTermination(9, TimeUnit.SECONDS)));
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
    future.get(); // CancellationException
  }

  @Test
  public void testInterruptFutureTask() throws InterruptedException {
    pool = Executors.newCachedThreadPool();
    InterruptableTask task = new InterruptableTask();
    Future<String> future = pool.submit(task);
    task.blockUntilStarted();
    future.cancel(true); // set true to interrupt the running thread, no exception is thrown from here
    Assertions.assertThat(task.blockUntilInterrupted()).isFalse(); // thread.sleep() is not interrupted, hence returns false
  }

  @Test
  public void testCompletableFutureCancel() {
    pool = Executors.newWorkStealingPool();
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
      try {
        for (int i = 0; i < 999; i++) {
          System.out.println("sleep...");
          TimeUnit.MILLISECONDS.sleep(99);
        }
      } catch (InterruptedException e) {
        Throwables.throwIfUnchecked(e);
      }
      return "this is a message";
    }, pool);
    final boolean isCancelled = future.cancel(false);
    log.info("isCancelled: {}", isCancelled);

  }

  @Test
  public void testSplit() {
    final Optional<String> mime = Optional.ofNullable("image/jpeg;charset=UTF-8");
    final Matcher m = Pattern.compile("\\w+/([^;]+)").matcher(mime.orElse(""));
    if (m.find()) {
      System.out.println(m.group(1));
    }

    final Integer firstIndex = mime.filter(s -> s.contains("/")).map(s -> s.indexOf('/') + 1).orElse(-1);
    final Integer sencondIndex = mime.filter(s -> s.contains(";")).map(s -> s.indexOf(';')).orElse(-1);
    if (firstIndex > 0 && sencondIndex > 0 && firstIndex < sencondIndex) {
      System.out.println(mime.map(s -> s.substring(firstIndex, sencondIndex)).orElse(""));
    }
  }

  @Test
  public void testPtn() {
    final Matcher r = Pattern.compile("[A-Za-z0-9\\-]+").matcher("CMP-012-70cabb8e-db92-4b4e-8352-a4ad038d4065 \n sss");
    System.out.println(r.matches());

  }


}
