package sample.basic;

import com.google.common.collect.ImmutableList;
import io.vavr.collection.HashMap;
import io.vavr.collection.Iterator;
import io.vavr.collection.Vector;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class LstTest {

  @Test
  public void testEmpty() {
    System.out.println(Lst.empty());
    Assertions.assertThatThrownBy(() -> Lst.empty().head()).isInstanceOf(NoSuchElementException.class);
    Assertions.assertThatThrownBy(() -> Lst.empty().tail()).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void testCons() {
    System.out.println(Lst.empty().cons("test").cons("a").cons("b"));
    System.out.println(Lst.of(1, 2, 3, 4, 5));
    StreamSupport.stream(Lst.of(1, 2, 3, 4, 5).spliterator(), true).forEach(System.out::println);
  }

  @Test
  public void testPrepend() {
    System.out.println(Lst.of(1, 2).prepend(Lst.of(3, 4, 5)).prepend(Lst.of(6, 7)));
  }

  @Test
  public void testFlatMap() {
    System.out.println(Lst.of(Lst.of(1, 2), Lst.of(3, 4), Lst.of(5, 6)).flatMap(Function.identity()));
  }

  @Test
  public void testRetainAll() {
    final Set<Integer> set1 = Vector.ofAll(1, 2, 3, 4, 5, 6, 7, 8).toJavaSet();
    final Set<Integer> set2 = Vector.ofAll(2, 4, 0).toJavaSet();
    final boolean set3 = set1.retainAll(set2);
    System.out.println(set1);

    final java.util.HashMap<Integer, String> map1 = HashMap.<Integer, String>empty().put(1, "a").put(2, "b").put(3, "c").put(4, "c").put(5, "d").toJavaMap();
    map1.keySet().retainAll(set2);
    System.out.println(map1);
    final List<String> list = Vector.of("a", "b", "a", "c").toJavaList();
    list.remove("a");
    System.out.println(list);
    double a = 2.0D;
    int b = (int) a;
    final String s = "starg".substring(2, 5);
    System.out.println(s);
    boolean b1 = true, b2 = false;
    int i1 = 1, i2 = 2;
    System.out.println(i1 | i2);
    System.out.println("aaa".getClass().getSimpleName());
    System.out.println(Boolean.parseBoolean("ssss"));
  }

  @Test
  public void wordCount() {
    List<Entry<String, Integer>> entries = ImmutableList
        .copyOf("This is a test This is another test This is another test  This is test3  This is a test4 ".split("\\s+"))
        .stream()
        .collect(Collectors.toMap(Function.identity(), word -> 1, Integer::sum))
        .entrySet()
        .stream()
        .sorted(Comparator.<Entry<String, Integer>>comparingInt(Entry::getValue).reversed().thenComparing(Entry::getKey))
        .collect(Collectors.toList());
    System.out.println(entries);
  }


  public static String extractFsa(final String postCode) {
    return Optional.ofNullable(postCode).filter(s -> StringUtils.length(s) > 3).map(s -> StringUtils.lowerCase(StringUtils.substring(s, 0, 3))).orElse(null);
  }
  @Test
  public void testSliding() {
    Vector<Vector<Integer>> vectors = Vector.of(1, 2, 3, 4, 5, 6, 7, 8, 9).sliding(2).toVector();
    System.out.println(vectors);
    Vector<Vector<Integer>> perms = Vector.of(1, 2, 3).permutations();
    System.out.println(perms);
    System.out.println(extractFsa("V5xcccc"));
    System.out.println(extractFsa("V5"));
    System.out.println(extractFsa("x58f"));

  }
}