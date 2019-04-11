package sample.jooq;

import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


@Slf4j
public class DbUtil {
  private final DSLContext dslContext;
  private final String sql;
  private final Object[] bindings;

  private DbUtil(@NonNull final DSLContext dslContext, @NonNull final String sql, final Object[] bindings) {
    this.dslContext = dslContext;
    this.sql = sql;
    this.bindings = bindings;
  }

  public static DbUtil withConnectionAndSql(@NonNull final Connection connection, @NonNull final String sql) {
    return new DbUtil(DSL.using(connection), sql, null);
  }

  public static DbUtil withSql(@NonNull final String sql) {
    return new DbUtil(DSL.using(DataSourceUtil.getConnection()), sql, null);
  }

  public DbUtil withBindings(@NonNull final Object... bindings) {
    return new DbUtil(dslContext, sql, bindings);
  }

  public <T> List<T> fetch(@NonNull RecordMapper<? super Record, T> mapper) {
    if (Objects.isNull(bindings)) {
      return Try.success(dslContext)
        .mapTry(dslContext -> dslContext.fetch(sql))
        .mapTry(result -> result.map(mapper))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(ImmutableList.of());
    } else {
      return Try.success(dslContext)
        .mapTry(dslContext -> dslContext.fetch(sql, bindings))
        .mapTry(result -> result.map(mapper))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(ImmutableList.of());
    }
  }

  public <T> Optional<T> fetchSingle(@NonNull Function<? super Record, T> mapper) {
    if (Objects.isNull(bindings)) {
      return Try.success(dslContext)
        .mapTry(dslContext -> dslContext.fetchOptional(sql))
        .mapTry(r -> r.map(mapper))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(Optional.empty());
    } else {
      return Try.success(dslContext)
        .mapTry(dslContext -> dslContext.fetchOptional(sql, bindings))
        .mapTry(r -> r.map(mapper))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(Optional.empty());
    }
  }

  public int execute() {
    if (Objects.isNull(bindings)) {
      return Try.success(dslContext)
        .mapTry(dslContext -> dslContext.execute(sql))
        .onFailure(t -> log.error("execute sql error: {}", t))
        .getOrElse(-1);
    } else {
      return Try.success(dslContext)
        .mapTry(dslContext -> dslContext.execute(sql, bindings))
        .onFailure(t -> log.error("execute sql error: {}", t))
        .getOrElse(-1);
    }
  }
}

