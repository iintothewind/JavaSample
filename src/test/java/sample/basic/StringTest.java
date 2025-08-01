package sample.basic;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sample.csv.bean.ManifestBrief;
import sample.http.JsonUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class StringTest {

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
        int a1 = StringUtils.indexOf(s, "echobase-ear");
        int a2 = StringUtils.indexOf(s.substring(a1), "/");
        final String subStr = StringUtils.substring(s.substring(a1), 0, a2);
        System.out.println(subStr);
    }

    @Test
    public void testReminder() {
        log.info("reminder: {}", 0 % ((int) Math.pow(10, 3)));
    }


    @Test
    public void testManifestBrief() {
        final ManifestBrief m1 = ManifestBrief.builder().manifest("YEG-24031212").route(13640).driver(2670).number(97).build();
        final ManifestBrief m2 = ManifestBrief.builder().manifest("YEG-24031212").route(13640).driver(2671).number(58).build();
        final ManifestBrief m3 = ManifestBrief.builder().manifest("YUL-24031310").route(13660).driver(2302).number(33).build();
        final ManifestBrief m4 = ManifestBrief.builder().manifest("YUL-24031310").route(13663).driver(2409).number(22).build();

        final List<ManifestBrief> manifests = ImmutableList.of(m1, m2, m3, m4);
        final Map<String, Integer> routeMap = manifests.stream()
            .collect(Collectors.toMap(m -> Tuple.of(m.getManifest(), m.getRoute()), m -> 1, (l, r) -> l))
            .entrySet().stream()
            .collect(Collectors.toMap(kv -> kv.getKey()._1, Entry::getValue, Integer::sum));
        System.out.println(routeMap);
        final Map<String, Integer> driverMap = manifests.stream()
            .collect(Collectors.toMap(m -> Tuple.of(m.getManifest(), m.getDriver()), m -> 1, (l, r) -> l))
            .entrySet().stream()
            .collect(Collectors.toMap(kv -> kv.getKey()._1, kv -> kv.getValue(), (l, r) -> l + r));
        System.out.println(driverMap);

        final List<Tuple3<String, Integer, Integer>> manifestBriefs = routeMap.keySet().stream().map(k -> Tuple.of(k, routeMap.getOrDefault(k, 0), driverMap.getOrDefault(k, 0))).collect(Collectors.toList());
        System.out.printf("manifestBriefs: %s%n", manifestBriefs);
//        final Map<String, Integer> driverMap = manifests.stream().collect(Collectors.toMap(ManifestBrief::getManifest, m -> m.getNumber(), Integer::sum));

    }

    @Test
    public void testRegexMatch001() {
        final boolean result = Pattern.matches("^[\\d,]+$", "999");
        System.out.println(result);

    }

    @Test
    public void testLocalTime() {
        final LocalTime localTime = LocalTime.of(6, 0, 0);
        final Map<String, LocalTime> map = HashMap.of("time", localTime).toJavaMap();
        JsonUtil.dump(map);
    }

    @Test
    public void testTake() {
        final List<Integer> lst = Stream.from(0).take(0).toJavaList();
        System.out.println(lst);
    }

    @Test
    public void testValueOfEnum() {
        final boolean result = ImmutableList.of(1, 2, 3).contains(null);
        System.out.println(result);
        TestEnum.valueOf("aaa");
    }

    @Test
    public void testOption() {
        // Option.map() may produce NPE, because Option.Some(null) is accepted
        // final Integer r = Option.of(Tuple.<Integer, Integer, Integer>of(1, null, null)).map(t ->t._2).map(i -> i * 2).getOrElse(0);
        final Integer r = Option.of(Tuple.<Integer, Integer, Integer>of(1, null, null)).flatMap(t -> Option.of(t._2)).map(i -> i * 2).getOrElse(0);
        System.out.println(r);
    }

    public static Duration getRemainingDay() {
        return Duration.between(LocalDateTime.now(), LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));
    }

    public static Duration getRemainingMinute() {
        return Duration.between(LocalDateTime.now(), LocalDateTime.now().withSecond(59));
    }

    @Test
    public void testGetRemainingDuration() {
        final Duration dayRemaining = getRemainingDay();
        System.out.println(dayRemaining.getSeconds());
        final Duration minRemaining = getRemainingMinute();
        System.out.println(minRemaining.getSeconds());

        final String s = String.join(":", ImmutableList.of("1", "2", "3", "4"));
        System.out.println(s);
    }

    @Test
    public void testUniq01() {
        final List<String> ltianFsas = List.of();
        final Set<String> uniqFsas = ltianFsas.stream().collect(Collectors.toSet());
        log.info("lstSize: {}, uniqSize: {}", ltianFsas.size(), uniqFsas.size());
    }

    @Test
    public void testCheckPOBox01() {
        final Pattern pxPattern = Pattern.compile("p[.,。\\s]*[0o][#@&/.,。\\s]*b[0o]x");
        final Pattern boxPattern = Pattern.compile("b[0o]x#*\\d+");
        final List<String> poboxes = ImmutableList.of("pobox", "p0box", "pob0x", "p0b0x", "po.box", "p0.box", "po.b0x", "p0.b0x", "po。box", "p0。box", "po。b0x", "p0。b0x", "po/box", "p0/box", "po/b0x", "p0/b0x", "pobox", "p0box", "pob0x", "p0b0x", "po&box", "p0&box", "po&b0x", "p0&b0x", "po@box", "p0@box", "po@b0x", "p0@b0x", "po#box", "p0#box", "po#b0x", "p0#b0x", "p.obox", "p.0box", "p.ob0x", "p.0b0x", "p,obox", "p,0b0x", "p,ob0x", "p,0b0x", "p。obox", "p。0box", "p。ob0x", "p。0b0x", "p.o.box", "p.0.box", "p.o.b0x", "p.0.b0x", "p,o.box", "p,0.box", "p,o.b0x", "p,0.b0x", "p.o,box", "p.0,box", "p.o,b0x", "p.0,b0x", "p,o,box", "p,0.box", "p,o,b0x", "p,0,b0x", "p。o.box", "p。0.box", "p。o.b0x", "p。0.b0x", "p.o。box", "p.0。box", "p.o。b0x", "p.0。b0x", "p。o。box", "p。0.box", "p。o。b0x", "p。0。b0x", "box1", "b0x2", "box#3", "b0x#4");
        final boolean result = poboxes.stream().map(s -> String.format("%s %s %s", "prefix", s, "suffix")).allMatch(s -> pxPattern.matcher(s).find() || boxPattern.matcher(s).find());
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testRegexGroup01() {
        final Pattern p = Pattern.compile("^\\[([0-9,]*)]$");
        final String input = "[1,33,34,35,580,716,481,1332,1347,1349,1351,1369,1462,1504,1506]";
        final Matcher matcher = p.matcher(input);

        if (matcher.find()) {
            final String subString = matcher.group(1);
            System.out.println(subString);
        }
    }

}
