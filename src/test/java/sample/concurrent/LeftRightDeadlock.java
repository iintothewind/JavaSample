package sample.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class LeftRightDeadlock {
    private ExecutorService pool;

    @Before
    public void setUp() {
        pool = Executors.newCachedThreadPool();
    }

    @After
    public void teardown() throws InterruptedException {
        pool.awaitTermination(10, TimeUnit.SECONDS);
        pool.shutdown();
    }

    @Test
    public void testReentrantDeadlock() throws InterruptedException {
        final ReentrantDeadLock deadlock = new ReentrantDeadLock();
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deadlock.leftRight();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deadlock.rightLeft();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }


    @Test
    public void testIntrinsicDeadlock() throws InterruptedException {
        final IntrinsicDeadlock deadlock = new IntrinsicDeadlock();
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deadlock.leftRight();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deadlock.rightLeft();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }
}

final class ReentrantDeadLock {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private final Lock left = new ReentrantLock();
    private final Lock right = new ReentrantLock();

    public void leftRight() throws InterruptedException {
        if (left.tryLock()) {
            try {
                TimeUnit.SECONDS.sleep(3);
                if (right.tryLock()) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        doSomething();
                    } finally {
                        right.unlock();
                    }
                }
            } finally {
                left.unlock();
            }
        }
    }

    public void rightLeft() throws InterruptedException {
        if (right.tryLock()) {
            try {
                TimeUnit.SECONDS.sleep(3);
                if (left.tryLock()) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        doSomething();
                    } finally {
                        left.unlock();
                    }
                }
            } finally {
                right.unlock();
            }
        }
    }


    void doSomething() {
        log.info("called by left");
    }

    void doSomethingElse() {
        log.info("called by right");
    }
}

final class IntrinsicDeadlock {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private final Object left = new Object();
    private final Object right = new Object();

    public void leftRight() throws InterruptedException {
        synchronized (left) {
            TimeUnit.SECONDS.sleep(3);
            synchronized (right) {
                TimeUnit.SECONDS.sleep(3);
                doSomething();
            }
        }
    }

    public void rightLeft() throws InterruptedException {
        synchronized (right) {
            TimeUnit.SECONDS.sleep(3);
            synchronized (left) {
                TimeUnit.SECONDS.sleep(3);
                doSomethingElse();
            }
        }
    }

    void doSomething() {
        log.info("called by left");
    }

    void doSomethingElse() {
        log.info("called by right");
    }
}
