package sample.lambda.basic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class SubLst {
  private Logger log = LogManager.getLogger();

  public static List<Integer> maxSubList(List<Integer> list) {
    List<Integer> maxSublst = ImmutableList.of();
    List<Integer> tmpSublst = Lists.newLinkedList();
    List<Integer> lst = Optional.of(list).orElse(Lists.newLinkedList());
    for (Integer element : lst) {
      int tmpSum = tmpSublst.stream().reduce((sum, num) -> sum + num).orElse(0) + element;
      if (tmpSum > 0) {
        tmpSublst.add(element);
        if (tmpSum > maxSublst.stream().reduce((sum, num) -> sum + num).orElse(0)) {
          maxSublst = ImmutableList.copyOf(tmpSublst);
        }
      } else {
        tmpSublst = Lists.newLinkedList();
      }
    }
    return maxSublst;
  }

  @Test
  public void testMaxSubList() {
    log.info(maxSubList(Lists.newArrayList(-2, 1, -3, 4, -1, 2, 1, -5, 4)));
    log.info(maxSubList(Lists.newArrayList(9, -2, 1, -3, 4, -1, 2, 1, -5, 11, 4)));
    log.info(maxSubList(Lists.newArrayList(-2, 1, -3)));
  }

  @Test
  public void testSum() {
    log.info(Lists.newArrayList(1, 2, 3).stream().reduce((sum, num) -> sum + num).orElse(0));
  }

}
