package sample.lambda.basic;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class StringSample {
  @Test
  public void testString() {
    System.out.println(String.join("-", "2014", "1", "1"));
    System.out.println(String.join("-", ImmutableList.of("2014", "1", "1")));
  }
}
