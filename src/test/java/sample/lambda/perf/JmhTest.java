package sample.lambda.perf;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import io.codearte.jfairy.Fairy;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@State(Scope.Benchmark)
public class JmhTest {
  private final int LEN = 10000;
  private final Fairy fairy = Fairy.create();
  private final List<Integer> list = Lists.newArrayListWithExpectedSize(LEN);

  public JmhTest() {
    for (int i = 0; i < LEN; i++) {
      list.add(fairy.person().getDateOfBirth().getEra());
    }
  }

  @Benchmark
  @Fork(value = 1)
  public Integer getMaxByFor() {
    int max = Integer.MIN_VALUE;
    for (int i = 0; i < list.size(); i++) {
      max = Integer.max(max, list.get(i));
    }
    //System.out.println(max);
    return max;
  }

  @Benchmark
  @Fork(value = 1)
  public Integer getMaxByForeach() {
    int max = Integer.MIN_VALUE;
    for (int i : list) {
      max = Integer.max(max, i);
    }
    //System.out.println(max);
    return max;
  }

  @Benchmark
  @Fork(value = 1)
  public Integer getMaxByLambda() {
    int max = list.stream().parallel().max(Ordering.natural()).get();
    //System.out.println(max);
    return max;
  }

}
