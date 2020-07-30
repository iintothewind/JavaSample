package sample.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import io.vavr.control.Try;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonQuerySample {
  private final String sample = "[\n" +
    "   {\n" +
    "      \"name\" : \"john\",\n" +
    "      \"grade\" : \"1.0\",\n" +
    "      \"gender\" : \"male\"\n" +
    "   },\n" +
    "   {\n" +
    "      \"name\" : \"ben\"\n" +
    "   }\n" +
    "]\n";

  public static String readLines(final String path) {
    return Try.of(() -> Files.readAllLines(Paths.get(ClassLoader.getSystemResource(path).toURI())))
      .map(list -> String.join("\n", list))
      .onFailure(Throwable::printStackTrace)
      .getOrElse("");
  }

  static <T> T query(final String json, final String path, final TypeRef<T> typeRef) {
    final Configuration conf = Configuration.builder().mappingProvider(new JacksonMappingProvider()).build();
    return JsonPath.using(conf).parse(json).read(path, typeRef);
  }

//  static String query(final String json, final String path) {
//    final Configuration conf = Configuration.builder().mappingProvider(new JacksonMappingProvider()).build();
//    return JsonPath.using(conf).parse(json).read(path);
//  }

  @Test
  public void testBasic() {
    Configuration conf = Configuration.builder().mappingProvider(new JacksonMappingProvider()).build();
    final String gender = JsonPath.using(conf).parse(sample).read("$[0]['gender']");
    final double grade = JsonPath.using(conf).parse(sample).read("$[0]['grade']", new TypeRef<Double>() {
    });
    System.out.println(gender);
    System.out.println(grade);

  }

  @Test
  public void testGenericQuery() {
    final Double gender = query(sample, "$[0]['grade']", new TypeRef<Double>() {
    });
    System.out.println(gender);
  }

  @Test
  public void testLoadEntries() {
    System.out.println(readLines("entries.json"));
  }
}
