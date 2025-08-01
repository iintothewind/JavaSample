package sample.concurrent;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class SubTask implements Callable<String> {
  private final ExecutorService pool;

  public SubTask(ExecutorService pool) {
    this.pool = pool;
  }

  @Override
  public String call() throws Exception {
    // before get() returned, it needs an extra thread to execute the subtask
    return Try.of(() -> pool.submit(() -> "pass").get(100, TimeUnit.MILLISECONDS)).getOrElse("fail");
  }
}

@Slf4j
public class ThreadStarvationLockTest {
  private ExecutorService pool = null;

  @BeforeEach
  public void setUp() {

  }

  @AfterEach
  public void tearDown() {
    Optional.of(pool).ifPresent((p) -> {
      try {
        p.awaitTermination(500, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void testSyncQueuedFixedThreadPool() throws ExecutionException, InterruptedException {
    pool = Executors.newFixedThreadPool(1); // only one thread is running the task, the cause of thread starvation, can be avoided by use Future.get() with timeout
    Future<String> future = pool.submit(new SubTask(pool));
    Assertions.assertThat(future.get()).contains("fail");
  }

  @Test
  public void testSyncQueuedPool() throws ExecutionException, InterruptedException {
    // SynchronousQueue has a queue with size of 0, so all extra tasks will be aborted or discarded
    pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());
    Future<String> future = pool.submit(new SubTask(pool));// root cause: RejectedExecutionException when ThreadPoolExecutor.AboardPolicy is used
    Assertions.assertThat(future.get()).contains("fail");
  }

  @Test
  public void testSyncQueuedWorkStealingPool() throws ExecutionException, InterruptedException {
    pool = Executors.newWorkStealingPool(1);
    Future<String> future = pool.submit(new SubTask(pool));
    Assertions.assertThat(future.get()).contains("pass");
  }
}
