package sample.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.testng.annotations.Test;

import java.util.regex.Pattern;


@Slf4j
public class EventBusSample {

  final EventBus eventBus = new EventBus((exception, context) -> log.warn(exception.getMessage()));

  @Subscribe
  public void recordCustomerChange(String event) {
    log.info("receiving event: {}", event);
  }

  @Test
  public void testEventBus() {
    eventBus.register(this);
    eventBus.post("msg");
  }

  private static final Pattern FILE_TAG_PATTERN = Pattern.compile("^\\w+(\\|\\w+)*$");
  private static final Pattern FILE_TAG_PATTERN2 = Pattern.compile("^\\w+$");

  @Test
  public void testFileTag() {
    val matched = FILE_TAG_PATTERN2.matcher("tag1\ntag_3").matches();

    System.out.println(matched);

  }

}
