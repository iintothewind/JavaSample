package sample.foundation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.vavr.control.Option;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class Loop {

  public int fibSlow(int n) {
    return Option.of(n).filter(i -> i > 1).map(a -> fibSlow(a - 2) + fibSlow(a - 1)).getOrElse(n);
  }

  public int fibLoop(int n) {
    if (n < 2) return n;
    int fibn = 0;
    int fibnm1 = 1;
    int fibnm2 = 0;

    for (int i = 2; i <= n; i++) {
      fibn = fibnm2 + fibnm1;
      fibnm2 = fibnm1;
      fibnm1 = fibn;
    }
    return fibn;
  }

  public int fibTailRec(int n, int a, int b) {
    if (n == 0) {
      return a;
    } else {
      return fibTailRec(n - 1, b, a + b);
    }
  }

  @Test
  public void testFib() {
    System.out.println(fibSlow(0));
    System.out.println(fibSlow(1));
    System.out.println(fibSlow(9));
    System.out.println(fibLoop(0));
    System.out.println(fibLoop(1));
    System.out.println(fibLoop(40));
    System.out.println(fibTailRec(0, 0, 1));
    System.out.println(fibTailRec(1, 0, 1));
    System.out.println(fibTailRec(40, 0, 1));
  }

  @Test(expected = ConcurrentModificationException.class)
  public void removeElementsInList() {
    List<String> list = Lists.newArrayList("a", "b", "c", "d", "e");
    for (String s : list) {
      list.remove(s);
    }
  }

  @Test
  public void removeElementsInIteration() {
    List<String> list = Lists.newArrayList("a", "b", "c", "d", "e");
    Iterator<String> iterator = list.iterator();
    while (iterator.hasNext()) {
      String s = iterator.next();
      if (s.equals("c")) {
        iterator.remove();
      }
    }
    System.out.println(list);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void removeElementsInQueue() {
    Queue<String> queue = Lists.newLinkedList(ImmutableList.of("a", "b", "c", "d", "e"));
    for (String s : queue) {
      if (s.equals("c")) {
        queue.remove();
      }
    }

  }
}
