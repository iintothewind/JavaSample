package sample.basic;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import io.vavr.collection.Vector;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class StringSample {

    @Test
    public void testString() {
        System.out.println(String.join("-", "2014", "1", "1"));
        System.out.println(String.join("-", ImmutableList.of("2014", "1", "1")));

        final String s = "1028_roy_tony";
        final String ss = s.substring(s.indexOf("_") + 1);
        System.out.println(ss);
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
            .map(s -> Vector.of(s.replaceAll("[\\\\().\\-_&%@\\[\\]]", " ").split("\\s+")).last())
            .collect(Collectors.toList());
        System.out.println(newIds);
    }

    @Test
    public void extractDriverIdSuffix() {
        System.out.println(StringUtils.substring("test_test_test_test_", 0, 18));
        System.out.println(StringUtils.substring("test", 0, 1));
        System.out.println(Splitter.on("_").splitToList("1028_Roy-Tony").stream().findFirst().map(Ints::tryParse).orElse(0));
    }

    @Test
    public void testD1D2() {
        final List<String> d1 = ImmutableList.copyOf(ResourceUtil.readResource("classpath:d1.txt").split("\r\n"));
        final List<String> d2 = ImmutableList.copyOf(ResourceUtil.readResource("classpath:d2.txt").split("\r\n"));
        final List<String> activeDs = d1.stream().filter(d2::contains).collect(Collectors.toList());
        System.out.println(String.join(",", activeDs));
        System.out.println(LocalDateTime.now().getDayOfWeek().getValue());
    }

    @Test
    public void testExtractEarFileName() {
        final String s = "jar:file:/C:/tools/payara41/glassfish/domains/domain1/applications/echobase-ear/echobase-web_war/WEB-INF/lib/echobase-ejb-0.0.2-SNAPSHOT.jar!/regions.json";
        int a1 = StringUtils.indexOf(s, "applications/");
        int a2 = StringUtils.indexOf(s, "/echobase-web");
        final String subStr = StringUtils.substring(s, a1 + "applications/".length(), a2);
        System.out.println(subStr);

    }

}
