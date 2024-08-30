package sample.concurrent;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimingThreadPoolExecutor extends ThreadPoolExecutor {
  private final AtomicLong numTasks = new AtomicLong();
  private final AtomicLong totalTime = new AtomicLong();
  private final ThreadLocalStopWatch watch = ThreadLocalStopWatch.createUnstarted();

  public TimingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
  }

  public TimingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
  }

  public TimingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }

  public TimingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    try {
      watch.reset();
      watch.start();
    } finally {
      super.beforeExecute(t, r);
    }
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    try {
      watch.stop();
      long taskTime = watch.elapsed(TimeUnit.MILLISECONDS);
      numTasks.incrementAndGet();
      totalTime.addAndGet(taskTime);
      this.log.debug("Task Time: " + taskTime + ", Total Task Time: " + totalTime.get());
    } finally {
      super.afterExecute(r, t);
    }
  }

  @Override
  protected void terminated() {
    try {
      log.info(String.format(
        "Terminated: avg task time: " + totalTime.get() + "/" + numTasks.get() + " == %d ms",
        totalTime.get() / numTasks.get()));
    } finally {
      super.terminated();
    }
  }
}
