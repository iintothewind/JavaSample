package sample.lambda.stream;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import sample.lambda.bean.Person;

import java.io.IOException;

public class StreamSample {
    private final Logger log = LogManager.getLogger();
    Person ivar, ashley, sara, jim, leon;

    @Test
    public void testForeach() throws IOException {
        ImmutableList
                .copyOf(new PathMatchingResourcePatternResolver().getResources("classpath*:**/*.xml"))
                .forEach(res -> log.info(res.getFilename()));
    }
}
