package sample.basic;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;


@Slf4j
public class CacheUtil {

    private final static Cache<Tuple2<Integer, String>, Integer> sorterScanCache = CacheBuilder
            .newBuilder()
            .maximumSize(99999L)
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .build();

    public void reduceDuplicates(Integer id, String code) {
        final Tuple2<Integer, String> tuple = Tuple.of(id, code);
        @Nullable final Integer number = sorterScanCache.getIfPresent(tuple);
        if (number!=null) {
            log.info("found duplicates for: {}", tuple);
        }
        sorterScanCache.put(tuple, 1);
    }

    @Test
    public void testGet() {
        reduceDuplicates(null, null);
        reduceDuplicates(null, null);
        reduceDuplicates(null, null);
        reduceDuplicates(1, "001");
        reduceDuplicates(1, "001");
        reduceDuplicates(1, "001");
        reduceDuplicates(1, "001");
    }

    @Test
    public void testLoadCache01() {
        final Cache<String, Integer> cache = CacheBuilder.newBuilder()
                .build(CacheLoader.from(s->Integer.parseInt(s)));

        final Integer val01 = cache.getIfPresent("abc");
        final Integer val02 = cache.getIfPresent("abc");
        final Integer val03 = cache.getIfPresent("ab");
        final Integer val04 = cache.getIfPresent("ab");

    }
}
