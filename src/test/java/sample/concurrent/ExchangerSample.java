package sample.concurrent;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class ExchangerSample {
    private final Logger log = LogManager.getLogger();
    private Map<String, String> map;
    private Exchanger<String> exchanger;
    Properties prop = new Properties();

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

final class Publisher implements Runnable {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
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

final class Talker implements Runnable {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
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
        String message = new String();
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
