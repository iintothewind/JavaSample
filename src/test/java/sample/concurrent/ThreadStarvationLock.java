package sample.concurrent;

import javaslang.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.*;

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

public class ThreadStarvationLock {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private ExecutorService pool = null;

    @Before
    public void setUp() {

    }

    @After
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
