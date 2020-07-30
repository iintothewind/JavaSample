package sample.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class EventBus {
  private final ConcurrentHashMap<String, Subject> subjects = new ConcurrentHashMap<>();

  public String subscribe(final Subject subject) {
    final UUID uuid = UUID.randomUUID();
    this.subjects.put(uuid.toString(), subject);
    return uuid.toString();
  }

  public void post(final String id, final String message) {
    final Subject subject = this.subjects.get(id);
    if (subject != null) {
      subject.execute(message);
    } else {
      log.info("dead message: {}", message);
    }
  }
}
