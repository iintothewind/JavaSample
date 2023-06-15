package sample.csv.basic;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sample.csv.bean.Country;
import sample.http.ResourceUtil;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
public class Sample {
    private Reader reader;
    private Stopwatch watch;

    @Before
    public void setUp() {
        reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/country1.csv")));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testOpenCsv() throws IOException {
        watch = Stopwatch.createStarted();
        Function<String[], Country> function = (String[] input) -> new Country(Integer.parseInt(input[0]), input[1], input[2]);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:country1.csv");
        try (CSVReader csvReader = new CSVReader(new FileReader(resource.getFile()), ';', '"', 1)) {
            csvReader.readAll().forEach((record) -> {
                System.out.println(Transformer.with(function).transform(record));
            });
        }
        watch.stop();
        System.out.println(String.format("Cost: %s MILLISECONDS", watch.elapsed(TimeUnit.MILLISECONDS)));
    }

    @Test
    public void testByteCodeCsv() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:country1.csv");
        Function<String[], Country> function = (String[] input) -> new Country(Integer.parseInt(input[0]), input[1], input[2]);
        CSV csv = CSV.separator(';').skipLines(1).create();
        csv.read(resource.getFile().getCanonicalPath(), (int index, String[] record) -> {
            System.out.println(Transformer.with(function).transform(record));
        });
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

    @Test
    @SneakyThrows
    public void testLoadErrorData() {
        final Resource rows = ResourceUtil.loadResource("classpath:errorData.csv");
        final ImmutableList.Builder<Order> builder = new ImmutableList.Builder<>();

        CSV csv = CSV.separator(',').skipLines(1).create();
        csv.read(rows.getInputStream(), (int index, String[] record) -> {
            final Order order = Order.builder()
                .trackNumber(record[0])
                .orderId(record[1])
                .title(record[2])
                .phoneNumber("5147952470")
                .email(record[4])
                .zipCode(record[8])
                .address(String.format("%s,%s,%s %s", record[5], record[6], record[7], record[8]))
                .notes("")
                .consent("")
                .weight("1.452")
                .length("29.30")
                .width("20.10")
                .height("7.90")
                .build();
            log.info("index: {}, record: {}", index, order);
            builder.add(order);
        });
        final ImmutableList<Order> orders = builder.build();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:errorOrders.csv");
        File f = new File("./output.csv");
        csv.write(f, writer -> {
            writer.writeAll(orders.stream().map(o -> new String[] { o.getTrackNumber(), o.getOrderId(), o.getTitle(), o.getPhoneNumber(), o.getEmail(), o.getZipCode(), o.getAddress(), o.getNotes(), o.getConsent(), o.getWeight(), o.getLength(), o.getWidth(), o.getHeight() }).collect(Collectors.toList()));
        });

    }
}
