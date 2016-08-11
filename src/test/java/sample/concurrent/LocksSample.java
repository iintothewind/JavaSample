package sample.concurrent;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class LocksSample {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private final Random random = new Random();
    private Map<String, String> map = null;
    private ExecutorService pool = null;
    private ReentrantLock reentrantLock = null;
    private ReadWriteLock readWriteLock = null;
    private StampedLock stampedLock = null;

    @Before
    public void setUp() {
        this.map = Maps.newHashMap();
        this.pool = Executors.newWorkStealingPool();
        this.reentrantLock = new ReentrantLock();
        this.readWriteLock = new ReentrantReadWriteLock();
        this.stampedLock = new StampedLock();
    }

    @After
    public void tearDown() throws InterruptedException {
        this.pool.awaitTermination(3, TimeUnit.SECONDS);
        this.pool.shutdownNow();
    }

    @Test
    public void testReentrantLockSupport() {
        this.pool.submit(() -> {
            try {
                reentrantLock.lockInterruptibly();
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.warn(e.getMessage());
            } finally {
                reentrantLock.unlock();
            }
        });
        this.pool.submit(() -> {
            log.info("Locked: {}", reentrantLock.isLocked());
            log.info("Held by me: {}", reentrantLock.isHeldByCurrentThread());
            log.info("Lock acquired: {}", reentrantLock.tryLock());
        });
    }

    @Test
    public void testTryLock() {
        this.pool.submit(() -> {
            try {
                reentrantLock.tryLock(100, TimeUnit.MILLISECONDS);
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.warn(e.getMessage());
            } finally {
                reentrantLock.unlock();
            }
        });

        this.pool.submit(() -> {
            log.info("Locked: {}", reentrantLock.isLocked());
            log.info("Held by me: {}", reentrantLock.isHeldByCurrentThread());
            //this.pool.
            log.info("Lock acquired: {}", reentrantLock.tryLock());

        });
    }

    private Runnable getRwlockWriteTask(String v) {
        return () -> {
            readWriteLock.writeLock().lock();
            try {
                log.info("put foo -> {}", v);
                map.put("foo", v);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        };
    }

    private Runnable getRwlockReadTask(String k) {
        return () -> {
            readWriteLock.readLock().lock();
            try {
                log.info("get {}", map.get(k));
            } finally {
                readWriteLock.readLock().unlock();
            }
        };
    }

    @Test
    public void testReadWriteLock() {
        pool.submit(getRwlockWriteTask("alpha"));
        pool.submit(getRwlockReadTask("foo"));
        pool.submit(getRwlockWriteTask("beta"));
        pool.submit(getRwlockReadTask("foo"));
        pool.submit(getRwlockWriteTask("gamma"));
        pool.submit(getRwlockReadTask("foo"));
        pool.submit(getRwlockWriteTask("omega"));
        pool.submit(getRwlockReadTask("foo"));
    }

    private Runnable getStampedLockWriteTask(String v) {
        return () -> {
            long stamp = stampedLock.writeLock();
            try {
                log.info("put foo -> {}", v);
                map.put("foo", v);
            } finally {
                stampedLock.unlockWrite(stamp);
            }
        };
    }

    private Runnable getStampedLockReadTask(String k) {
        return () -> {
            long stamp = stampedLock.readLock();
            try {
                log.info("get {}", map.get(k));
            } finally {
                stampedLock.unlockRead(stamp);
            }
        };
    }

    private Runnable getStampedLockOptimisticWriteTask(String v) {
        return () -> {
            long stamp1 = stampedLock.tryOptimisticRead();
            log.info("tryOptimisticRead stamp = {}", stamp1);
            long stamp2 = stampedLock.tryConvertToWriteLock(stamp1);
            try {
                if (stampedLock.validate(stamp2)) {
                    log.info("converted To WriteLock stamp = {}", stamp2);
                    log.info("put foo -> {}", v);
                    map.put("foo", v);
                } else {
                    log.info("failed To convert to WriteLock stamp = {}", stamp2);
                }
            } finally {
                stampedLock.unlock(stamp1);
                stampedLock.unlock(stamp2);
            }
        };
    }


    @Test
    public void testStampedLock() {
        pool.submit(getStampedLockWriteTask("001"));
        pool.submit(getStampedLockReadTask("foo"));
        pool.submit(getStampedLockWriteTask("002"));
        pool.submit(getStampedLockReadTask("foo"));
        pool.submit(getStampedLockWriteTask("003"));
        pool.submit(getStampedLockOptimisticWriteTask("009"));
        pool.submit(getStampedLockReadTask("foo"));
        pool.submit(getStampedLockWriteTask("004"));
        pool.submit(getStampedLockReadTask("foo"));
    }
}
