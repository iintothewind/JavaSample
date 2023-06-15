package sample.basic;

import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CacheUtil {

    private final static Cache<Tuple2<Integer, String>, Integer> sorterScanCache = CacheBuilder
        .newBuilder()
        .maximumSize(99999L)
        .expireAfterWrite(1L, TimeUnit.DAYS)
        .build();

    public void reduceDuplicates(Integer id, String code) {
        final Tuple2<Integer, String> tuple = Tuple.of(id, code);
        @Nullable
        final Integer number = sorterScanCache.getIfPresent(tuple);
        if (number != null) {
            log.info("found duplicates for: {}",tuple);
        }
        sorterScanCache.put(tuple, 1);
    }

    @Test
    public void testGet() {
        reduceDuplicates(null,null);
        reduceDuplicates(null,null);
        reduceDuplicates(null,null);
        reduceDuplicates(1,"001");
        reduceDuplicates(1,"001");
        reduceDuplicates(1,"001");
        reduceDuplicates(1,"001");
    }
}
