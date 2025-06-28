package sample.http;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlUtil {

    private final static XmlMapper mapper = new XmlMapper();

    static {
        mapper.findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T load(final String source, @NonNull TypeReference<T> typeRef) {
        return Try.<T>of(() -> mapper.readValue(source, typeRef))
                .onFailure(t -> log.error("XmlUtil.load, failed to load from source: {}", source, t))
                .getOrNull();
    }

    public static String dump(final Object obj) {
        return Try.of(() -> mapper.writeValueAsString(obj))
                .onFailure(t -> log.error("XmlUtil.dump, failed to dump object: {}", obj, t))
                .getOrNull();
    }

}
