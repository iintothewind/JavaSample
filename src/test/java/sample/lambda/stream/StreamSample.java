package sample.lambda.stream;

import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import sample.lambda.bean.Person;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class StreamSample {
  Person ivar, ashley, sara, jim, leon;
  private List<Tuple2<Integer, String>> lst = ImmutableList.of(Tuple.of(1, "a"), Tuple.of(1, "b"), Tuple.of(2, "c"), Tuple.of(2, "d"), Tuple.of(3, "e"));

  @Test
  public void testForeach() throws IOException {
    ImmutableList
      .copyOf(new PathMatchingResourcePatternResolver().getResources("classpath*:**/*.xml"))
      .forEach(res -> log.info(res.getFilename()));
  }

  public static String nodeToStr(ListNode ln) {
    StringBuilder sb = new StringBuilder();
    while (ln != null) {
      sb.append(ln.val);
      ln = ln.next;
    }

    return sb.reverse().toString();
  }

  @Test
  public void testCollectMap() {
    final Map<Integer, List<String>> m = lst
      .stream()
      .collect(Collectors.toMap(
        t -> t._1,
        t -> ImmutableList.of(t._2),
        (l, r) -> Stream.concat(l, r).collect(Collectors.toList())));
    System.out.println(m);
  }

  @Test
  public void testGroupBy() {
    final Map<Integer, List<String>> m1 = lst
      .stream()
      .collect(Collectors.groupingBy(
        t -> t._1,
        Collectors.mapping(t -> t._2, Collectors.toList())));
    System.out.println(m1);
    final Map<Integer, Optional<String>> m2 = m1
      .entrySet()
      .stream()
      .map(kv -> Tuple.of(kv.getKey(), kv.getValue().stream().max(Comparator.<String>naturalOrder())))
      .collect(Collectors.toMap(t -> t._1, t -> t._2));
    System.out.println(m2);
  }

  @Test
  public void testGroupByThenMax() {
    final Map<Integer, Optional<String>> m = lst
      .stream()
      .collect(Collectors.groupingBy(
        t -> t._1,
        Collectors.mapping(t -> t._2, Collectors.maxBy(Comparator.<String>naturalOrder()))));
    System.out.println(m);
    Collections.reverse(ImmutableList.of(1, 2, 3));
//    String.join("", ImmutableList.of("1", "2", "3"))
  }

  public ListNode strToNode(String s) {
    ListNode h = new ListNode(0);
    ListNode n = h;
    for (char c : new StringBuilder(s).reverse().toString().toCharArray()) {
      h.next = new ListNode(Integer.parseInt(String.valueOf(c)));
      h = h.next;
    }
    return n.next;
  }

  public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummyHead = new ListNode(0);
    ListNode p = l1, q = l2, curr = dummyHead;
    int carry = 0;
    while (p != null || q != null) {
      int x = p == null ? 0 : p.val;
      int y = q == null ? 0 : q.val;
      curr.next = new ListNode((carry + x + y) % 10);
      carry = (x + y + carry) / 10;
      curr = curr.next;
      if (p != null) p = p.next;
      if (q != null) q = q.next;
    }
    if (carry > 0) {
      curr.next = new ListNode(carry);
    }

    return dummyHead.next;
  }

  @Test
  public void testStr() {
    final String result = nodeToStr(addTwoNumbers(strToNode("1"), strToNode("99")));
    System.out.println(result);

  }

  public static class ListNode {
    int val;
    ListNode next;

    ListNode(int x) {
      val = x;
    }
  }
}
