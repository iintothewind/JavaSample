package sample.concurrent;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;


import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

public class CyclicBarrierSample {
    @Test
    public void testRace() {
        CarRace race = new CarRace();
        try {
            race.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

final class CarRace implements Runnable {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private final int numberOfCars = Runtime.getRuntime().availableProcessors();
    private final BlockingQueue<Car> fleet;
    private final int trackLength;
    private final ExecutorService pool;
    private final Random random;
    private CyclicBarrier barrier;

    public CarRace() {
        fleet = new LinkedBlockingQueue<Car>(numberOfCars);
        trackLength = 20000;
        pool = Executors.newFixedThreadPool(numberOfCars);
        random = new Random();
    }

    public int getTrackLength() {
        return this.trackLength;
    }

    public CyclicBarrier getBarrier() {
        return this.barrier;
    }

    public BlockingQueue<Car> getFleet() {
        return this.fleet;
    }

    public void start() throws InterruptedException {
        barrier = new CyclicBarrier(numberOfCars, this);
        for (int i = 0; i < numberOfCars; i++) {
            pool.execute(new Car("Car" + i, 100 + random.nextInt(100), this));
        }
        pool.awaitTermination(300, TimeUnit.MILLISECONDS);
    }

    public void report() {
        this.log.info("fleet: " + Ordering.natural().immutableSortedCopy(fleet));
    }

    @Override
    public void run() {
        report();
    }

}

final class Car implements Runnable, Comparable<Car> {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private final String name;
    private final Integer speed;
    private Long timeElapsed;
    private final CarRace carRace;

    public Car(String name, Integer speed, CarRace carRace) {
        this.name = name;
        this.speed = speed;
        this.carRace = carRace;
    }

    public String getName() {
        return this.name;
    }

    public Long getTimeElapsed() {
        return this.timeElapsed;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("speed", speed).add("timeElapsed", timeElapsed)
                .toString();
    }

    @Override
    public int compareTo(Car car) {
        Preconditions.checkNotNull(car);
        Preconditions.checkState(timeElapsed != 0L, "Car didn't join the race.");
        Preconditions.checkState(car.getTimeElapsed() != 0L, "Car didn't join the race.");
        return ComparisonChain.start().compare(timeElapsed, car.getTimeElapsed()).result();
    }

    @Override
    public void run() {
        Stopwatch watch = Stopwatch.createStarted();
        int progress = 0;
        while (progress < carRace.getTrackLength()) {
            progress = progress + speed;
        }
        watch.stop();

        timeElapsed = watch.elapsed(TimeUnit.MICROSECONDS);
        try {
            carRace.getFleet().put(this);
            log.debug("Car: " + name + " has reached the finish line, time cost in microseconds : " + timeElapsed);
            carRace.getBarrier().await();
        } catch (InterruptedException | BrokenBarrierException e) {
            this.log.warn(e.getMessage());
        }
    }

}