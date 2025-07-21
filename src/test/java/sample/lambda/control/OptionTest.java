package sample.lambda.control;

import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Patterns.$None;
import static io.vavr.Patterns.$Some;


public class OptionTest {

  @Test
  public void testPartialFunction() {
    final Option<String> opt = Option.of("abc");
    final String toUpper = Match(opt).of(
      Case($Some($(s -> s.startsWith("a"))), String::toUpperCase),
      Case($None(), "NONE")
    );
    Assertions.assertThat(toUpper).isEqualTo("ABC");
  }

}
