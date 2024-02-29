package sample.basic;

import com.google.common.collect.ImmutableList;
import io.vavr.collection.Vector;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class StringSample {

  @Test
  public void testString() {
    System.out.println(String.join("-", "2014", "1", "1"));
    System.out.println(String.join("-", ImmutableList.of("2014", "1", "1")));
  }

  @Test
  public void testNumFormat() {
    final Long maxDigits = 4L;
    final String format = String.format("%%d%%0%dd_%%s", maxDigits - 1);
    System.out.println(format);
    System.out.println(String.format(format, 1, 1234567, "test"));
  }

  @Test
  public void testReplace() {
    final String driverIds = """
        1085-Wen(Wendy)
        1083-YuenYun(Vicky)
        ftd.chi
        1
        Roy - Rafael
        2006e-HARKIRAT RAJ SINGH
        2024-YYC-A&W
        test_a
        jjjjjjj__asdfsadf
        asdf%ssdf
        bb@asdfsdf
        asdf[ss]
        """;

    final List<String> newIds = ImmutableList.copyOf(driverIds.split("\n"))
        .stream()
        .map(s -> Vector.of(s.replaceAll("[\\\\(\\\\)\\.\\-_&%@\\[\\]]", " ").split("\\s+")).last())
        .collect(Collectors.toList());
    System.out.println(newIds);
  }

  @Test
  public void extractDriverIdSuffix() {
    System.out.println(StringUtils.substring("test_test_test_test_", 0, 18));
    System.out.println(StringUtils.substring("test", 0, 1));
  }

}
