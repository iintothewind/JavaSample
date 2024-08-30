package sample.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

@Slf4j
public class ExchangerSample {
  Properties prop = new Properties();
  private Map<String, String> map;
  private Exchanger<String> exchanger;

  @Before
  public void startUp() throws IOException {
    prop.load(this.getClass().getResourceAsStream("/asc.properties"));
    map = Maps.fromProperties(prop);

    exchanger = new Exchanger<String>();
  }

  @Test
  public void testLoad() {
    map.forEach((k, v) -> {
      log.debug("{} -> {}", k, v);
    });
  }

  @Test
  public void testExchanger() throws InterruptedException {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    pool.execute(new Publisher(exchanger, map));
    pool.execute(new Talker(exchanger, map));

    pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
  }
}

@Slf4j
final class Publisher implements Runnable {
  private final Exchanger<String> exchanger;
  private final Map<String, String> map;

  public Publisher(Exchanger<String> exchanger, Map<String, String> map) {
    this.exchanger = exchanger;
    this.map = map;
  }

  public Map<String, String> getMap() {
    return this.map;
  }

  public Exchanger<String> getExchanger() {
    return this.exchanger;
  }

  @Override
  public void run() {
    Preconditions.checkNotNull(map);
    String message = null;
    for (String key : map.keySet()) {
      try {
        message = exchanger.exchange(key, 20, TimeUnit.MILLISECONDS);
      } catch (InterruptedException | TimeoutException e) {
        break;
      }
      if (!Strings.isNullOrEmpty(message) && !message.endsWith("=null")) {
        this.log.info(message);
      }
    }
    log.debug("Publisher exit.");
  }
}

@Slf4j
final class Talker implements Runnable {
  private final Exchanger<String> exchanger;
  private final Map<String, String> map;

  public Talker(Exchanger<String> exchanger, Map<String, String> map) {
    this.exchanger = exchanger;
    this.map = map;
  }

  public Map<String, String> getMap() {
    return this.map;
  }

  public Exchanger<String> getExchanger() {
    return this.exchanger;
  }

  @Override
  public void run() {
    Preconditions.checkNotNull(map);
    String key = "start";
    String message = "";
    while (!Strings.isNullOrEmpty(key)) {
      try {
        key = exchanger.exchange(key + "=" + map.get(key), 20, TimeUnit.MILLISECONDS);
      } catch (InterruptedException | TimeoutException e) {
        break;
      }
    }
    log.debug("Talker exit.");
  }
}
