package sample.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EventBusTest {

  @After
  public void tearDown() throws InterruptedException {
    TimeUnit.SECONDS.sleep(3);
  }

  @Test
  public void testSend() {
    final EventBus eventBus = new EventBus();
    final ExecutorService pool = Executors.newWorkStealingPool();
    Subject subject1 = new Subject() {
      @Override
      public void execute(final String message) {
        log.info("subject1: {}", message);
      }
    };

    Subject subject2 = new Subject() {
      @Override
      public void execute(final String message) {
        log.info("subject2: {}", message);
      }
    };
    final String subject1Id = eventBus.subscribe(subject1);
    final String subject2Id = eventBus.subscribe(subject2);

    pool.submit(() -> eventBus.post(subject1Id, "this is the message for subject 1"));
    pool.submit(() -> eventBus.post(subject2Id, "this is the message for subject 2"));
  }
}
