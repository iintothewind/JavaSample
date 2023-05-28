package sample.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.NonNull;

import java.util.Optional;

public interface JsonUtil {
  ObjectMapper objectMapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static <T> Optional<T> load(@NonNull final String source, @NonNull TypeReference<T> typeRef) {
    return Try.of(() -> objectMapper.readValue(source, typeRef)).toJavaOptional();
  }

  public static Optional<String> dump(final Object obj) {
    return Try.of(() -> objectMapper.writeValueAsString(obj)).toJavaOptional();
  }
}
