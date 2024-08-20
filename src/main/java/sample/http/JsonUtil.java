package sample.http;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.Charset;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JsonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

    public static <T> T load(final String source, @NonNull TypeReference<T> typeRef) {
        return Try.of(() -> objectMapper.readValue(source, typeRef))
            .onFailure(t -> log.error("failed to load from source: {}", source, t))
            .getOrNull();
    }

    public static String dump(final Object obj) {
        return Try.of(() -> objectMapper.setSerializationInclusion(Include.NON_NULL).writeValueAsString(obj))
            .onFailure(t -> log.error("failed to dump object: {}", obj, t))
            .getOrNull();
    }

    /**
     * helper method for <code>java.net.http.HttpRequest.BodyPublisher</code>
     *
     * @param obj any object
     * @return a StringBodyPublisher for input object
     */
    public static <T> BodyPublisher publisherOf(final T obj) {
        return BodyPublishers.ofString(dump(obj));
    }

    /**
     * helper method for <code>java.net.http.HttpResponse.BodyHandler</code>
     *
     * @param typeRef the json TypeReference for to be deserialized
     * @return a BodyHandler to deserialize http response into the typeRef type
     */
    public static <T> BodyHandler<T> handlerOf(@NonNull TypeReference<T> typeRef) {
        return responseInfo -> Optional
            .ofNullable(responseInfo)
            .filter(r -> r.statusCode() < 299)
            .map(r -> BodySubscribers.mapping(BodySubscribers.ofByteArray(),
                bytes -> Try.of(() -> objectMapper.readValue(bytes, typeRef))
                    .onFailure(e -> log.error("failed to load response: ", e))
                    .getOrNull()))
            .orElse(BodySubscribers.mapping(BodySubscribers.ofString(Charset.defaultCharset()), error -> {
                log.error("failed to handle non successful response, status: {}, body: {}", Optional.ofNullable(responseInfo).map(ResponseInfo::statusCode).orElse(-1), error);
                return null;
            }));
    }
}
