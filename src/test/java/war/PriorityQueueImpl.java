package war;

import sample.basic.PriorityQueue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PriorityQueueImpl implements PriorityQueue {
  private final List<Entry> list = new ArrayList<>();

  @Override
  public void enqueue(final int data, final int priority) {
    list.add(Entry.builder().priority(priority).index(list.size() + 1).data(data).build());
  }

  @Override
  public int dequeue() {
    final Optional<Entry> entry = list.stream().min(Comparator.comparing(Entry::getPriority).thenComparing(Entry::getIndex));
    entry.ifPresent(list::remove);
    return entry.map(Entry::getData).orElse(-1);
  }


}
