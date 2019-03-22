package sample.jooq;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DbUtil {
    private final Connection connection;
    private final String sql;
    private final Object[] bindings;

    private DbUtil(final Connection connection, final String sql, final Object[] bindings) {
        this.connection = connection;
        this.sql = sql;
        this.bindings = bindings;
    }

    public static DbUtil withSql(@NonNull final String sql) {
        return new DbUtil(DataSourceUtil.getConnection(), sql, null);
    }

    public DbUtil withBindings(@NonNull final Object... bindings) {
        return new DbUtil(connection, sql, bindings);
    }

    public <T> List<T> fetch(@NonNull RecordMapper<? super Record, T> mapper) {
        if (Objects.isNull(bindings)) {
            return Try.of(() -> DSL.using(connection))
                .mapTry(dslContext -> dslContext.fetch(sql))
                .mapTry(result -> result.map(mapper))
                .onFailure(t -> log.error("fetch records error: {}", t))
                .getOrElse(ImmutableList.of());
        } else {
            return Try.of(() -> DSL.using(connection))
                .mapTry(dslContext -> dslContext.fetch(sql, bindings))
                .mapTry(result -> result.map(mapper))
                .onFailure(t -> log.error("fetch records error: {}", t))
                .getOrElse(ImmutableList.of());
        }
    }

    public <T> Optional<T> fetchSingle(@NonNull Function<? super Record, T> mapper) {
        if (Objects.isNull(bindings)) {
            return Try.of(() -> DSL.using(connection))
                .mapTry(dslContext -> dslContext.fetchOptional(sql))
                .mapTry(r -> r.map(mapper))
                .onFailure(t -> log.error("fetch records error: {}", t))
                .getOrElse(Optional.empty());
        } else {
            return Try.of(() -> DSL.using(connection))
                .mapTry(dslContext -> dslContext.fetchOptional(sql, bindings))
                .mapTry(r -> r.map(mapper))
                .onFailure(t -> log.error("fetch records error: {}", t))
                .getOrElse(Optional.empty());
        }
    }
}
