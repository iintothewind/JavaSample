package sample.csv.basic;


import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import sample.csv.bean.Country;

import java.io.*;
import java.util.concurrent.TimeUnit;


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

}
