package sample.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Objects;
import com.google.common.base.Ticker;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.*;

@Slf4j
public final class ThreadLocalStopWatch {
  private final Ticker ticker;
  private final ThreadLocal<Boolean> isRunning = new ThreadLocal<>();
  private final ThreadLocal<Long> elapsedNanos = new ThreadLocal<>();
  private final ThreadLocal<Long> startTick = new ThreadLocal<>();

  /**
   * Creates (but does not start) a new StopWatch using
   * {@link System#nanoTime} as its time source.
   */
  private ThreadLocalStopWatch() {
    this(Ticker.systemTicker());
  }

  /**
   * Creates (but does not start) a new StopWatch, using the specified time
   * source.
   */
  private ThreadLocalStopWatch(Ticker ticker) {
    this.ticker = checkNotNull(ticker, "ticker");
  }

  /**
   * Creates (but does not start) a new StopWatch using
   * {@link System#nanoTime} as its time source.
   */
  public static ThreadLocalStopWatch createUnstarted() {
    return new ThreadLocalStopWatch();
  }

  /**
   * Creates (but does not start) a new StopWatch, using the specified time
   * source.
   */
  public static ThreadLocalStopWatch createUnstarted(Ticker ticker) {
    return new ThreadLocalStopWatch(ticker);
  }

  /**
   * Creates (and starts) a new StopWatch using {@link System#nanoTime} as its
   * time source.
   */
  public static ThreadLocalStopWatch createStarted() {
    return new ThreadLocalStopWatch().start();
  }

  /**
   * Creates (and starts) a new StopWatch, using the specified time source.
   */
  public static ThreadLocalStopWatch createStarted(Ticker ticker) {
    return new ThreadLocalStopWatch(ticker).start();
  }

  private static TimeUnit chooseUnit(long nanos) {
    if (DAYS.convert(nanos, NANOSECONDS) > 0) {
      return DAYS;
    }
    if (HOURS.convert(nanos, NANOSECONDS) > 0) {
      return HOURS;
    }
    if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
      return MINUTES;
    }
    if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
      return SECONDS;
    }
    if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
      return MILLISECONDS;
    }
    if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
      return MICROSECONDS;
    }
    return NANOSECONDS;
  }

  private static String abbreviate(TimeUnit unit) {
    switch (unit) {
      case NANOSECONDS:
        return "ns";
      case MICROSECONDS:
        return "\u03bcs"; // μs
      case MILLISECONDS:
        return "ms";
      case SECONDS:
        return "s";
      case MINUTES:
        return "min";
      case HOURS:
        return "h";
      case DAYS:
        return "d";
      default:
        throw new AssertionError();
    }
  }

  /**
   * Returns {@code true} if {@link #start()} has been called on this
   * StopWatch, and {@link #stop()} has not been called since the last call to
   * {@code start()}.
   */
  public boolean isRunning() {
    return isRunning.get() == null ? false : isRunning.get();
  }

  /**
   * Starts the StopWatch.
   *
   * @return this {@code StopWatch} instance
   * @throws IllegalStateException if the StopWatch is already running.
   */
  public ThreadLocalStopWatch start() {
    checkState(!isRunning(), "This StopWatch is already running.");
    isRunning.set(true);
    startTick.set(ticker.read());
    return this;
  }

  /**
   * Stops the StopWatch. Future reads will return the fixed duration that had
   * elapsed up to this point.
   *
   * @return this {@code StopWatch} instance
   * @throws IllegalStateException if the StopWatch is already stopped.
   */
  public ThreadLocalStopWatch stop() {
    long tick = ticker.read();
    checkState(isRunning.get(), "This StopWatch is already stopped.");
    isRunning.set(false);
    elapsedNanos.set(tick - startTick.get());
    return this;
  }

  /**
   * Sets the elapsed time for this StopWatch to zero, and places it in a
   * stopped state.
   *
   * @return this {@code StopWatch} instance
   */
  public ThreadLocalStopWatch reset() {
    elapsedNanos.set(0L);
    isRunning.set(false);
    return this;
  }

  /**
   * set all ThreadLocal value objects to null for garbage collection
   */
  private void clear() {
    elapsedNanos.set(null);
    isRunning.set(null);
    startTick.set(null);
  }

  /**
   * @return the elapsed time in desired time unit for current thread if
   * ticker is set, or zero if the ticker and elapsed value are set to
   * null
   */
  private long elapsedNanos() {
    boolean isTickerReady = !Objects.equal(null, isRunning.get()) && !Objects.equal(null, startTick.get())
      && !Objects.equal(null, elapsedNanos.get());
    return isTickerReady ? (isRunning.get() ? ticker.read() - startTick.get() + elapsedNanos.get() : elapsedNanos
      .get()) : 0L;

  }

  /**
   * Returns the current elapsed time shown on this StopWatch, expressed in
   * the desired time unit, with any fraction rounded down.
   * <p>
   * <p>
   * Note that the overhead of measurement can be more than a microsecond, so
   * it is generally not useful to specify {@link java.util.concurrent.TimeUnit#NANOSECONDS}
   * precision here. <b> Note that the ticker and elapsed value will be
   * destroyed for garbage collection by calling {@link #clear()}<b>
   *
   * @return the elapsed time in desired time unit for current thread if
   * ticker is set, or zero if the ticker and elapsed value are set to
   * null
   */
  public long elapsed(TimeUnit desiredUnit) {
    try {
      return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
    } finally {
      clear();
    }
  }

  /**
   * Returns a string representation of the current elapsed time.
   */
  @GwtIncompatible("String.format()")
  @Override
  public String toString() {
    long nanos = elapsedNanos();

    TimeUnit unit = chooseUnit(nanos);
    double value = (double) nanos / NANOSECONDS.convert(1, unit);

    // Too bad this functionality is not exposed as a regular method call
    return String.format("%.4g %s", value, abbreviate(unit));
  }
}
