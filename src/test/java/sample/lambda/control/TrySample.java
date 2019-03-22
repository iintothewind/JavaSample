package sample.lambda.control;

import com.google.common.collect.ImmutableList;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;


public class TrySample {
  @Test
  public void testFlatMap() {
    Option.of("cfg/config01.conf")
      .toTry()
      // handle IOException and URISyntaxException internally
      .mapTry(s -> Files.readAllLines(Paths.get(ClassLoader.getSystemResource(s).toURI())))
      .getOrElse(ImmutableList.of())
      .forEach(System.out::println);
  }

  @Test
  public void testOptionToTry() {
    Option.of("test")
      .filter(s -> s.startsWith("a"))
      .toTry()
      .onFailure(e -> Assertions.assertThat(e).isInstanceOf(NoSuchElementException.class));
    Option.of("test")
      .filter(s -> s.startsWith("t"))
      .toTry()
      .onSuccess(s -> Assertions.assertThat(s).isEqualTo("test"));
  }

  @Test
  public void testFinally() {
    Try.of(() -> Integer.parseInt("100"))
      .onSuccess(i -> System.out.printf("parsedInt: %s \n", i))
      .onFailure(e -> Assertions.assertThat(e).isInstanceOf(NumberFormatException.class))
      .andFinally(() -> System.out.println("finally always run"));
  }

}
