package sample.lambda.control;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import com.google.common.collect.ImmutableList;
import io.vavr.control.Option;


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

}
