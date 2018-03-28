package sample.lambda.basic;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.sample.lambda.basic.Lst;
import java.util.NoSuchElementException;
import java.util.function.Function;
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


}
