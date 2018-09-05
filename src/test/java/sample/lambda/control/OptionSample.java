package sample.lambda.control;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Patterns.$None;
import static io.vavr.Patterns.$Some;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import io.vavr.control.Option;


public class OptionSample {

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
