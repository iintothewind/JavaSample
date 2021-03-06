package sample.validation;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public interface ValidationUtils {
  static <T, E> Validation<T, E> check(final Predicate<T> predicate, final E error) {
    Objects.requireNonNull(predicate, "predicate is required");
    Objects.requireNonNull(error, "error message is required");
    return (T input) -> {
      if (predicate.test(input)) {
        return Collections.emptyList();
      } else {
        return Collections.singletonList(error);
      }
    };
  }

  static <T, E> Validation<T, E> check(final Predicate<T> predicate, final Function<T, E> function) {
    Objects.requireNonNull(predicate, "predicate is required");
    Objects.requireNonNull(function, "error message is required");
    return (T input) -> {
      if (predicate.test(input)) {
        return Collections.emptyList();
      } else {
        return Collections.singletonList(function.apply(input));
      }
    };
  }

  static <T> Validation<Iterable<T>, String> checkAll(final Validation<T, String> v) {
    return (Iterable<T> iterable) -> StreamSupport.stream(iterable.spliterator(), true)
      .flatMap(t -> {
        try {
          return StreamSupport.stream(v.validate(t).spliterator(), true);
        } catch (Throwable throwable) {
          return Stream.of(throwable.getMessage());
        }
      }).distinct().collect(Collectors.toList());
  }

  static <T> Validation<T, String> checkEquals(final T expected, final String error) {
    return check(actual -> Objects.equals(expected, actual), error);
  }

  static Validation<String, String> checkStringNotEmpty(final String error) {
    return check(input -> Optional.ofNullable(input).filter(s -> !s.isEmpty()).isPresent(), error);
  }

}
