package sample.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscribers;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JsonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T load(final String source, @NonNull TypeReference<T> typeRef) {
        return Try.of(() -> objectMapper.readValue(source, typeRef))
            .onFailure(t -> log.error("failed to load from source: {}", source, t))
            .getOrNull();
    }

    public static String dump(final Object obj) {
        return Try.of(() -> objectMapper.writeValueAsString(obj))
            .onFailure(t -> log.error("failed to dump object: {}", obj, t))
            .getOrNull();
    }

    public static <T> BodyPublisher publisherOf(final T obj) {
        return BodyPublishers.ofString(dump(obj));
    }

    public static <T> BodyHandler<T> handlerOf(@NonNull TypeReference<T> typeRef) {
        return responseInfo -> BodySubscribers.mapping(BodySubscribers.ofByteArray(), bytes -> Try.of(() -> objectMapper.readValue(bytes, typeRef))
            .onFailure(e -> log.error("failed to load response: ", e))
            .getOrNull());
    }
}
