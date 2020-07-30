package war;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Solution1 {

  public static List<Map.Entry<Integer, Integer>> sortLst(List<Integer> nums) {
    return Optional
      .ofNullable(nums)
      .orElse(ImmutableList.of())
      .stream()
      .collect(Collectors.toMap(Function.identity(), n -> 1, Integer::sum))
      .entrySet()
      .stream()
      .sorted(Comparator.<Map.Entry<Integer, Integer>>comparingInt(Map.Entry::getValue).thenComparing(Map.Entry::getKey))
      .collect(Collectors.toList());
  }

  public static int random() {
    return ThreadLocalRandom.current().nextInt();
  }

  public static int[] randomArray(int n) {
    final Map<Integer, Integer> result = new HashMap<>(n);
    for (int i = 0; i < n; i++) {
      while (true) {
        int randomNum = random();
        if (result.getOrDefault(randomNum, 0) == 0) {
          result.put(randomNum, 1);
          break;
        }
      }
    }
    return result.keySet().stream().mapToInt(i -> i).toArray();
  }

  @Test
  public void testSort() {
    final List<Map.Entry<Integer, Integer>> sorted = sortLst(ImmutableList.of(5, 5, 5, 4, 4, 2, 1, 0, 0, 0));
    System.out.println(sorted);
  }

  @Test
  public void testGenArray() {
    final int[] array = randomArray(9);
    for (int i = 0; i < array.length; i++) {
      System.out.println(array[i]);
    }
  }
}
