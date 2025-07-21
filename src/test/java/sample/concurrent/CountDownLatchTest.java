package sample.concurrent;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CountDownLatchTest {
  private final CountDownLatch branchLatchB = new CountDownLatch(1);
  private final CountDownLatch branchLatchC = new CountDownLatch(2);
  private final CountDownLatch branchLatchD = new CountDownLatch(1);
  private final CountDownLatch branchLatchE = new CountDownLatch(3);

  private final Activity ab = new Activity("A-B", 1000, null, branchLatchB);
  private final Activity ad = new Activity("A-D", 2000, null, branchLatchD);
  private final Activity bc = new Activity("B-C", 2000, branchLatchB, branchLatchC);
  private final Activity dc = new Activity("D-C", 1000, branchLatchD, branchLatchC);
  private final Activity be = new Activity("B-E", 3000, branchLatchB, branchLatchE);
  private final Activity ce = new Activity("C-E", 2000, branchLatchC, branchLatchE);
  private final Activity de = new Activity("D-E", 4000, branchLatchD, branchLatchE);
  private final Activity ef = new Activity("E-F", 3000, branchLatchE, null);

  @Test
  public void testLatchSchedule() {
    List<Activity> activities = ImmutableList.of(ef, de, ce, be, dc, bc, ad, ab);
    ExecutorService pool = Executors.newCachedThreadPool();
    for (Activity activity : activities) {
      pool.execute(activity);
    }

    pool.shutdown();
    try {
      pool.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testLatchScheduleWithTimingPool() {
    List<Activity> activities = ImmutableList.of(ef, de, ce, be, dc, bc, ad, ab);
    ExecutorService pool = new TimingThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
      new SynchronousQueue<Runnable>());
    for (Activity activity : activities) {
      pool.execute(activity);
    }

    pool.shutdown();
    try {
      pool.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

@Slf4j
final class Activity implements Runnable {
  private final String name;
  private final int duration;
  private final CountDownLatch start;
  private final CountDownLatch end;

  public Activity(String name, int duration, CountDownLatch start, CountDownLatch end) {
    this.name = name;
    this.duration = duration;
    this.start = start;
    this.end = end;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public void run() {
    try {
      Stopwatch watch = Stopwatch.createStarted();
      if (this.start != null) {
        try {
          this.start.await();
        } catch (InterruptedException e) {
          this.log.debug(e.getMessage());
        }
      }
      this.log.debug("task: " + this.getName() + " started");
      try {
        Thread.sleep(this.duration);
      } catch (InterruptedException e) {
        this.log.debug(e.getMessage());
      }
      watch.stop();
      this.log.info("Task: " + this.getName() + " costs: " + watch.elapsed(TimeUnit.MILLISECONDS));
    } catch (Throwable t) {
      if (t instanceof RuntimeException) {
        log.error("RuntimeException: " + t.getClass().getCanonicalName() + " occured while executing thread: "
          + Thread.currentThread().getName() + " with error message: " + t.getMessage());
      } else {
        throw new IllegalStateException("Error occured during executing thread", t);
      }
    } finally {
      if (this.end != null) {
        this.end.countDown();
      }
    }
  }

}