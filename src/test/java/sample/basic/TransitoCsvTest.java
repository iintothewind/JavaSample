package sample.basic;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;
import sample.http.ResourceUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TransitoCsvTest {
    @Test
    public void testLoadCsv() {
        final String txt = ResourceUtil.readResource("classpath:prod_desc_20230814.txt");
        final List<String> lines = ImmutableList.copyOf(txt.split("\r\n"));
        final File output = new File("src/test/resources/prod_desc_20230814.csv");
        Try.run(() -> Files.write(output.toPath(), lines.stream().map(s -> String.format("TEXT;%s;CAN;true;1", s)).collect(Collectors.toList()), StandardOpenOption.CREATE, StandardOpenOption.WRITE))
            .onFailure(Throwables::throwIfUnchecked);
    }

    @Test
    public void testDisplayDouble() {
        final double d = 3.1415926d;
        System.out.println(String.format("%.1f %s/task", d, "CAD"));
        final String s = "a,b, c, ";
        final ImmutableList<String> lst = ImmutableList.copyOf(Splitter.on(",").omitEmptyStrings().trimResults().split(""));
        System.out.println(lst);
        final Pattern stationRegex = Pattern.compile("^[a-zA-Z,]+$");
        final boolean matched = "sA,BBB".matches("^[a-zA-Z,]+$");
        System.out.println(matched);
    }

    @Test
    public void testToMap() {
        final String json = "[{\"region\":\"COQUITLAM\",\"completed\":1},{\"region\":\"New_West\",\"completed\":3},{\"region\":\"SURREY_NORTH\",\"completed\":1},{\"region\":\"PORT_MOODY\",\"completed\":2}]";
        System.out.println(json.length());
        final Map<String, Integer> sumMap = ImmutableList.of("a", "b", "b", "c", "c", "c", "d", "d", "d", "e")
            .stream().collect(Collectors.toMap(Function.identity(), s -> 1, Integer::sum));
        System.out.println(sumMap);
    }
}
