package sample.lambda.stream;

import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.collection.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import sample.lambda.bean.Person;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StreamSample {
  private final Logger log = LogManager.getLogger();
  Person ivar, ashley, sara, jim, leon;

  @Test
  public void testForeach() throws IOException {
    ImmutableList
      .copyOf(new PathMatchingResourcePatternResolver().getResources("classpath*:**/*.xml"))
      .forEach(res -> log.info(res.getFilename()));
  }

  @Test
  public void testCollectMap() {
    final Map<Integer, List<String>> m = ImmutableList
      .of(Tuple.of(1, "a"), Tuple.of(1, "b"), Tuple.of(2, "c"), Tuple.of(2, "d"), Tuple.of(3, "e"))
      .stream()
      .collect(Collectors.toMap(t -> t._1, t -> ImmutableList.of(t._2), (l, r) -> Stream.concat(l, r).collect(Collectors.toList())));
    System.out.println(m);
  }

  @Test
  public void testGroupBy() {
    final Map<Integer, List<String>> m1 = ImmutableList
      .of(Tuple.of(1, "a"), Tuple.of(1, "b"), Tuple.of(2, "c"), Tuple.of(2, "d"), Tuple.of(3, "e"))
      .stream()
      .collect(Collectors.groupingBy(t -> t._1, Collectors.mapping(t -> t._2, Collectors.toList())));
    System.out.println(m1);
    final Map<Integer, Optional<String>> m2 = m1.entrySet().stream().map(kv -> Tuple.of(kv.getKey(), kv.getValue().stream().max(Comparator.naturalOrder()))).collect(Collectors.toMap(t -> t._1, t -> t._2));
    System.out.println(m2);
  }

  @Test
  public void testGroupByThenMax() {
    final Map<Integer, Optional<String>> m = ImmutableList
      .of(Tuple.of(1, "a"), Tuple.of(1, "b"), Tuple.of(2, "c"), Tuple.of(2, "d"), Tuple.of(3, "e"))
      .stream()
      .collect(Collectors.groupingBy(t -> t._1, Collectors.mapping(t -> t._2, Collectors.maxBy(Comparator.naturalOrder()))));
    System.out.println(m);
  }
}
