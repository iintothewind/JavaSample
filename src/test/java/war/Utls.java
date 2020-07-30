package war;

import java.util.List;
import java.util.stream.Collectors;

public interface Utls {
  public static List<String> splitChars(final String str) {
    return str.chars().mapToObj(i -> String.valueOf((char) i)).collect(Collectors.toList());
  }

  public static List<Integer> splitIntChars(final String str) {
    return str.chars().mapToObj(i -> Integer.parseInt(String.valueOf((char) i))).collect(Collectors.toList());
  }
}
