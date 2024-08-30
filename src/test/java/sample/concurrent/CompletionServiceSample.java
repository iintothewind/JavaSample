package sample.concurrent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Slf4j
public class CompletionServiceSample {
  private final Integer numberOfHorses = 9;
  private final Long trackLength = Long.valueOf(20000L);
  private ExecutorService pool;
  private List<Horse> horses;
  private Random random;

  @Before
  public void start() {
    this.random = new Random();
    this.horses = Lists.newArrayList();
    this.pool = Executors.newCachedThreadPool();
  }

  @Test
  public void testCompletionService() {
    for (int i = 0; i < numberOfHorses; i++) {
      horses.add(new Horse("Horse" + i, 100 + random.nextInt(100), trackLength));
    }
    new HorseRace(pool, horses).start();
  }

}

@Slf4j
final class Horse implements Callable<Horse>, Comparable<Horse> {
  private final String name;
  private final Integer speed;
  private final Long distance;
  private Long timeElapsed;

  public Horse(String name, Integer speed, Long distance) {
    this.name = name;
    this.speed = speed;
    this.distance = distance;
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
  public int compareTo(Horse hourse) {
    Preconditions.checkNotNull(hourse);
    Preconditions.checkState(timeElapsed != 0L, "the hourse didn't join the race.");
    Preconditions.checkState(hourse.getTimeElapsed() != 0L, "the hourse didn't join the race.");
    return ComparisonChain.start().compare(timeElapsed, hourse.getTimeElapsed()).result();
  }

  @Override
  public Horse call() throws Exception {
    Stopwatch watch = Stopwatch.createStarted();
    Long progress = 0L;
    while (progress < distance) {
      progress = progress + speed;
      TimeUnit.MILLISECONDS.sleep(10);
    }
    watch.stop();

    timeElapsed = watch.elapsed(TimeUnit.MILLISECONDS);
    return this;
  }

}

@Slf4j
final class HorseRace {
  private final CompletionService<Horse> completionService;
  private List<Horse> horses;

  public HorseRace(ExecutorService executor, List<Horse> horses) {
    Preconditions.checkNotNull(executor);
    Preconditions.checkNotNull(horses);
    Preconditions.checkArgument(horses.size() > 0);
    this.horses = horses;
    this.completionService = new ExecutorCompletionService<Horse>(executor);
  }

  private void race() {
    for (Horse horse : horses) {
      completionService.submit(horse);
    }
  }

  public void list() {
    boolean interrupted = false;
    Future<Horse> future = null;
    for (int i = 0; i < horses.size(); i++) {
      try {
        future = completionService.take();
        log.info("Horse #" + i + " == " + future.get(1, TimeUnit.MICROSECONDS));
      } catch (TimeoutException e) {
        if (!Objects.equal(null, future)) {
          future.cancel(true);
        }
      } catch (InterruptedException e) {
        interrupted = true;
        continue;
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      } finally {
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public void start() {
    race();
    list();
  }

}
