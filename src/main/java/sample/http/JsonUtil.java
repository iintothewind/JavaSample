package sample.http;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import java.lang.reflect.Field;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Slf4j
public class JsonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        .setSerializationInclusion(Include.NON_NULL);

    public static <T> T load(final String source, @NonNull TypeReference<T> typeRef) {
        return Try.of(() -> objectMapper.readValue(source, typeRef))
            .onFailure(t -> log.error("JsonUtil.load, failed to load from source: {}", source, t))
            .getOrNull();
    }

    public static String dump(final Object obj) {
        return Try.of(() -> objectMapper.writeValueAsString(obj))
            .onFailure(t -> log.error("JsonUtil.dump, failed to dump object: {}", obj, t))
            .getOrNull();
    }

    public static <T> T diffObj(T oldObj, T newObj) {
        if (Objects.isNull(oldObj) || Objects.isNull(newObj) || !oldObj.getClass().equals(newObj.getClass())) {
            throw new IllegalArgumentException("Both objects must be non-null and of the same class.");
        }

        final Class<T> clazz = (Class<T>) newObj.getClass();

        T diffObj = objectMapper.convertValue(objectMapper.createObjectNode(), clazz);

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldValue = field.get(oldObj);
                Object newValue = field.get(newObj);

                if (!Objects.equals(oldValue, newValue)) {
                    field.set(diffObj, newValue);
                }
            } catch (IllegalAccessException e) {
                log.warn("JsonUtil.diffObj, failed to set field access: {}", field, e);
            }
        }

        return diffObj;
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
                    .onFailure(e -> log.error("JsonUtil.handlerOf, failed to load response: ", e))
                    .getOrNull()))
            .orElse(BodySubscribers.mapping(BodySubscribers.ofString(Charset.defaultCharset()), error -> {
                log.error("JsonUtil.handlerOf, failed to handle non successful response, status: {}, body: {}", Optional.ofNullable(responseInfo).map(ResponseInfo::statusCode).orElse(-1), error);
                return null;
            }));
    }

    public static BodyHandler<String> handlerOfString() {
        return responseInfo -> Optional
                .ofNullable(responseInfo)
                .filter(r -> r.statusCode() < 299)
                .map(r -> BodySubscribers.mapping(BodySubscribers.ofString(Charset.defaultCharset()), Function.identity()))
                .orElse(BodySubscribers.mapping(BodySubscribers.ofString(Charset.defaultCharset()), error -> {
                    log.error("JsonUtil.handlerOfString, failed to handle non successful response, status: {}, body: {}", Optional.ofNullable(responseInfo).map(ResponseInfo::statusCode).orElse(-1), error);
                    return null;
                }));
    }

    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class RespWrapper<T> {
        @EqualsAndHashCode.Include
        private Integer code;
        @EqualsAndHashCode.Include
        private String msg;
        @EqualsAndHashCode.Include
        private T data;

        public static boolean is2xx(final RespWrapper<?> wrapper) {
            return Optional.ofNullable(wrapper).filter(w -> w.getCode() >= 200 && w.getCode() < 299).isPresent();
        }

        public static String extractMsg(final RespWrapper<?> wrapper) {
            return Optional.ofNullable(wrapper).filter(w -> w.getCode() >= 400).map(RespWrapper::getMsg).orElse(null);
        }

        public static <T> T extractData(final RespWrapper<T> wrapper) {
            return Optional.ofNullable(wrapper).filter(w -> w.getCode() >= 200 && w.getCode() < 299).map(RespWrapper::getData).orElse(null);
        }
    }

    public static <T> HttpResponse.BodyHandler<RespWrapper<T>> handlerOfWrapper(@NonNull TypeReference<RespWrapper<T>> wrapper) {
        return responseInfo -> Optional
                .ofNullable(responseInfo)
                .filter(r -> r.statusCode() < 299)
                .map(r -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(Charset.defaultCharset()),
                        content -> JsonUtil.load(content, wrapper)))
                .orElse(HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(Charset.defaultCharset()), error -> {
                    final Integer statusCode = Optional.ofNullable(responseInfo).map(HttpResponse.ResponseInfo::statusCode).orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    log.error("JsonUtil.handlerOf, failed to handle non successful response, status: {}, body: {}", statusCode, error);
                    return RespWrapper.<T>builder().code(statusCode).msg(error).build();
                }));
    }
}
