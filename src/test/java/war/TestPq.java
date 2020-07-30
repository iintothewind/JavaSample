package war;

import org.junit.Test;

public class TestPq {
  @Test
  public void testDequeue() {
    final PriorityQueueImpl pq = new PriorityQueueImpl();
    pq.enqueue(1, 2);
    pq.enqueue(2, 2);
    pq.enqueue(3, 2);
    pq.enqueue(4, 1);
    pq.enqueue(5, 3);

    System.out.println(pq.dequeue());
    System.out.println(pq.dequeue());
    System.out.println(pq.dequeue());
    System.out.println(pq.dequeue());
    System.out.println(pq.dequeue());

  }
}
