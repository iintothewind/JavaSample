package war;


import org.junit.Test;

public class Vowels {
  public static int getCount(String str) {
    int vowelsCount = 0;
//    vowelsCount = Math.toIntExact(str.chars().filter(c -> "aeiou".contains(String.valueOf((char) c))).count());
    return str.replaceAll("(?i)[^aeiou]", "").length();
//    return vowelsCount;
  }

  @Test
  public void testCount() {
    System.out.println(getCount("nopeswell"));
  }
}
