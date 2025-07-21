package sample.sm;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UniuniTest {

  // input string: aabcdefc

  // return abcdef
  // "a","b","c",


  public static String findLongestUniqueString(final String inputs) {
    List<String> subStrings = new ArrayList<>();

    final List<String> lst = inputs.chars().mapToObj(ch -> Character.toString(ch)).collect(Collectors.toList());

    String k = "";

    for (final String ch : lst) {
      if (k.isEmpty()) {
        k = ch;
      } else if (!k.contains(ch)) {
        k = k.concat(ch);
      } else {
        subStrings.add(k);
        k = ch;
      }
    }

    return subStrings.stream().max(Comparator.comparing(String::length)).orElse("");
  }

  @Test
  public void testFindLongestUniqueString() {
    final String subStr = findLongestUniqueString("aabbabcedfgheeeeeeeedfsdsssssss");
    System.out.println(subStr);
    ;

  }


}
