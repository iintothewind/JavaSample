package sample.csv.bean;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.Map;

public abstract class CsvBeanTransformer<T> {
    private Function<Map<String, String>, T> function;

    protected CsvBeanTransformer(Function<Map<String, String>, T> function) {
        this.function = function;
    }

    public static <T> CsvBeanTransformer<T> with(Function<Map<String, String>, T> function) {
        return new CsvBeanTransformer<T>(function) {
        };
    }

    public T transform(Map<String, String> record) {
        return function.apply(record);
    }

    public FluentIterable<T> transform(Iterable<Map<String, String>> records) {
        return FluentIterable.from(records).transform(function);
    }

    public static <T> T transform(Map<String, String> record, Function<Map<String, String>, T> function) {
        return function.apply(record);
    }

    public static <T> T transform(Iterable<String> record, Function<Iterable<String>, T> function) {
        return function.apply(record);
    }

    public static <T> T transform(String[] record, Function<String[], T> function) {
        return function.apply(record);
    }

    public static <T> FluentIterable<T> mapRecordsTransform(Iterable<Map<String, String>> records, Function<Map<String, String>, T> function) {
        return FluentIterable.from(records).transform(function);
    }

    public static <T> FluentIterable<T> iterableRecordsTransform(Iterable<Iterable<String>> records, Function<Iterable<String>, T> function) {
        return FluentIterable.from(records).transform(function);
    }

    public static <T> FluentIterable<T> arrayRecordsTransform(Iterable<String[]> records, Function<String[], T> function) {
        return FluentIterable.from(records).transform(function);
    }
}
