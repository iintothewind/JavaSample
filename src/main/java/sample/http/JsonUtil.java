package sample.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JsonUtil {
    private final static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T load(@NonNull final String source, @NonNull TypeReference<T> typeRef) {
        return Try.of(() -> objectMapper.readValue(source, typeRef))
                .onFailure(t -> log.error("failed to load from source: {}", source, t))
                .getOrNull();
    }

    public static String dump(final Object obj) {
        return Try.of(() -> objectMapper.writeValueAsString(obj))
                .onFailure(t -> log.error("failed to dump object: {}", obj, t))
                .getOrNull();
    }
}
