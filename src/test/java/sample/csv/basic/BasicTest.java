package sample.csv.basic;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simpleflatmapper.csv.CsvParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import sample.csv.bean.Country;
import sample.http.ResourceUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


@Slf4j
public class BasicTest {

    private Reader reader;
    private Stopwatch watch;

    @BeforeEach
    public void setUp() {
        reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/country1.csv")));
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    @SneakyThrows
    public void testLoad01() {
        final String data = ResourceUtil.readResource("classpath:country1.csv");
        final List<Country> countries = CsvParser.separator(';').skip(1).reader(data).stream().map(row -> Country.builder()
                .id(Integer.parseInt(row[0]))
                .code(row[0])
                .name(row[1])
                .build())
            .toList();

        log.info("countries: {}", countries);
    }

    @Test
    @SneakyThrows
    public void testLoad02() {
        final String data = ResourceUtil.readResource("classpath:country1.csv");
        final List<Country> countries = CsvParser.separator(';')
            .mapTo(Country.class)
            .alias("country_id", "id")
            .alias("country_code", "code")
            .alias("country_name", "name")
            .stream(data)
            .toList();

        log.info("countries: {}", countries);
    }

    @Test
    public void testMerge() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/**/*.xml");
        for (Resource resource : resources) {
            System.out.println(resource.getURL());
        }
    }

    @Test
    public void testBiMap() {
        final ImmutableBiMap<String, String> map = ImmutableBiMap.<String, String>builder().putAll(Maps.filterKeys(ImmutableMap.of("1", "A", "2", "B"), i -> !Objects.equals(i, "1"))).put("1", "d").build();
        System.out.println(map);
    }

    @Test
    public void testMatch() {
        final boolean m = Pattern.compile("\\w+").matcher("asda-_fsd").matches();
        System.out.println(m);
    }
}
