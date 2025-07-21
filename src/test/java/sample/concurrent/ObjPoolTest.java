package sample.concurrent;

import io.vavr.collection.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import sample.pool.ObjPool;

import java.util.List;

@Slf4j
public class ObjPoolTest {
    @Test
    public void testConsume01() {
        final ObjPool<String> pool = new ObjPool<String>(List.of("1.1.1.1", "1.1.1.2", "1.1.1.3"));

        Stream.from(1L).take(999).toJavaList()
                .parallelStream()
                .forEach(n -> System.out.println(pool.<String>consume(s -> String.format("%s/%s", s, n))));

    }
}
