package sample.concurrent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DeadLockAvoidance {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private static AtomicLong counter;
    private static ExecutorService pool;
    private Account from, to;
    private Transfer transfer;

    @BeforeClass
    public static void init() {
        pool = Executors.newCachedThreadPool();
        counter = new AtomicLong(0);
    }

    @Before
    public void setUp() {
        this.from = new Account(9999L);
        this.to = new Account(0L);
        this.transfer = Transfer.getInstance();
    }

    @After
    public void tearDown() throws InterruptedException {
        pool.shutdown();
        this.log.info(counter.get() + " transactions completed.");
        this.log.info("from.balance == " + from.getBalance() + ", to.balance == " + to.getBalance());
    }

    @Test
    public void testTransfer() {
        for (int i = 0; i < 999; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    boolean successful = false;
                    try {
                        successful = transfer.transfer(from, to, 1L, TimeUnit.MILLISECONDS, 1L);
                    } catch (InsufficientFundsException | InterruptedException e) {
                        log.catching(Level.WARN, e);
                    }
                    if (successful) {
                        counter.incrementAndGet();
                    } else {
                        log.warn("transfer failed.");
                    }
                }
            });
        }
    }
}

final class Transfer {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private Random rnd = new Random();
    private static final int DELAY_FIXED = 1;
    private static final int DELAY_RANDOM = 3;

    private static class TransferHolder {
        private static Transfer instance = new Transfer();
    }

    public static Transfer getInstance() {
        return TransferHolder.instance;
    }

    public boolean transfer(Account from, Account to, long amount, TimeUnit unit, long timeout) throws InsufficientFundsException, InterruptedException {
        long stopTime = System.nanoTime() + unit.toNanos(timeout);

        while (true) {
            if (from.getLock().tryLock()) {
                try {
                    if (to.getLock().tryLock()) {
                        try {
                            if (from.getBalance() > amount) {
                                from.withdraw(amount);
                                to.deposit(amount);
                                return true;
                            } else {
                                throw new InsufficientFundsException();
                            }
                        } finally {
                            to.getLock().unlock();
                        }
                    }
                } finally {
                    from.getLock().unlock();
                }
            }
            TimeUnit.NANOSECONDS.sleep(DELAY_FIXED + (rnd.nextLong() % DELAY_RANDOM));
            if (System.nanoTime() > stopTime) {
                return false;
            }
        }
    }
}


final class Account {
    private final AtomicLong balance;
    private final Lock lock;

    public Account(long balance) {
        this.lock = new ReentrantLock();
        this.balance = new AtomicLong(balance);
    }

    public Lock getLock() {
        return lock;
    }

    public long getBalance() {
        return balance.get();
    }

    public long withdraw(long amount) {
        return this.balance.addAndGet(-amount);
    }

    public long deposit(long amount) {
        return this.balance.addAndGet(amount);
    }
}

final class InsufficientFundsException extends Exception {

}
