package sample.lambda.control;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static io.vavr.API.*;
import static io.vavr.Patterns.*;


public class MatchSample {

  @Test
  public void testSimpleMatch() {
    String s = "abc";
    String result = Match(s).of(
      Case($(a -> a.equalsIgnoreCase("bbb")), "yes"),
      Case($(), "?")
    );
    System.out.println(result);
  }

  @Test
  public void testOptionMatch() {
    final Option<String> opt = Option.of("abc");
    final String toUpper = Match(opt).of(
      Case($Some($(s -> s.startsWith("a"))), String::toUpperCase),
      Case($None(), "NONE")
    );
    Assertions.assertThat(toUpper).isEqualTo("ABC");
  }

  @Test
  public void testTryMatch() {
    final Integer n = Match(Try.of(() -> Integer.parseInt("abc123"))).of(
      Case($Success($(i -> i > 0)), i -> i),
      Case($Failure($()), e -> 0)
    );
    Assertions.assertThat(n).isEqualTo(0);
  }
}
