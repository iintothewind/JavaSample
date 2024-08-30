package sample.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class UncaughtSample {

    @Before
    public void setUp() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("System is shutting down.");
            }
        });
    }

    @Test
    public void testErrorHandlerThread() throws InterruptedException {
        ErrorHandlerThread test = new ErrorHandlerThread(5, 0);
        test.start();
        test.join();
    }

    @Test
    public void testRunByPool() throws InterruptedException {
        InPool test = new InPool(5, 0);
        ExecutorService pool = Executors.newCachedThreadPool();

        try {
            pool.execute(test);
        } finally {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.SECONDS);
        }

    }

}

@Slf4j
final class InPool implements Runnable {

    private final int numerator;
    private final int denominator;

    public InPool(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    private void exit() {
        this.log.debug("numerator == " + numerator + ", denominator == " + denominator);
    }

    @Override
    public void run() {
        try {
            int result = numerator / denominator;
            this.log.debug("numerator/denominator == " + result);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                log.error("RuntimeException: " + t.getClass().getCanonicalName() + " occured while executing thread: "
                    + Thread.currentThread().getName() + " with error message: " + t.getMessage());
            } else {
                throw new IllegalStateException("Error occured during executing thread", t);
            }
        } finally {
            this.exit();
        }
    }
}

@Slf4j
final class ErrorHandlerThread extends Thread implements UncaughtExceptionHandler {

    private final int numerator;
    private final int denominator;

    public ErrorHandlerThread(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public synchronized void start() {
        super.start();
        this.setUncaughtExceptionHandler(this);
    }

    @Override
    public void run() {
        int result = numerator / denominator;
        this.log.debug("numerator/denominator == " + result);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.warn("Error occured while executing thread: " + t.getName() + " with error message: " + e.getMessage());
        this.log.error("numerator == " + numerator + ", denominator == " + denominator);
    }

}
