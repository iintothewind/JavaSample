package sample.basic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.vavr.collection.Vector;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

@Slf4j
public class SubLst {

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
        log.info("lst: {}", maxSubList(Lists.newArrayList(-2, 1, -3, 4, -1, 2, 1, -5, 4)));
        log.info("lst: {}", maxSubList(Lists.newArrayList(9, -2, 1, -3, 4, -1, 2, 1, -5, 11, 4)));
        log.info("lst: {}", maxSubList(Lists.newArrayList(-2, 1, -3)));
    }

    @Test
    public void testSum() {
        log.info("lst: {}", Lists.newArrayList(1, 2, 3).stream().reduce(Integer::sum).orElse(0));
    }

    @Test
    public void testAvg() {
        final double avg = ImmutableList.of(0, 0, 0).stream().mapToDouble(i -> i).average().orElse(0D);
        System.out.println(avg);
        final String s = null;
        final String ss = Try.of(() -> s.substring(2)).getOrElse("");
        System.out.println(ss);
        final Integer a = null;
        final Integer b = 5;
        System.out.println(Try.of(() -> a * b).getOrElse(0));
        final List<String> lst = null;
        final Vector<String> vect = Vector.<String>ofAll(lst);
        System.out.println(vect);

    }

}
