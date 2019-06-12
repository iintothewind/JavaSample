package sample.jooq;

import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


@Slf4j
public class DbUtil {
  static {
    System.setProperty("org.jooq.no-logo", "true");
  }

  private final String sql;
  private final Object[] bindings;

  private final Connection connection;

  private DbUtil(@NonNull final Connection connection, @NonNull final String sql, final Object[] bindings) {
    this.connection = connection;
    this.sql = sql;
    this.bindings = bindings;
  }

  /**
   * @param connection use DataSourceConnectionUtil to get a connection from connection pool.
   *                   <b>Only resources allocated when you constructed the DSLContext will be released.
   *                   Not resources that you passed to the DSLContext.</b>
   *                   NOTE: You have to manually <b>close</b> the connection after using DSLContext
   * @param sql        statement, better to be prepared statement
   */
  public static DbUtil withSql(@NonNull final Connection connection, @NonNull final String sql) {
    return new DbUtil(connection, sql, null);
  }

  /**
   * fetch with default COMP db connection
   *
   * @param sql statement, better to be prepared statement
   */
  public static DbUtil withSql(@NonNull final String sql) {
    return withSql(DataSourceUtil.getConnection(), sql);
  }

  public DbUtil withBindings(@NonNull final Object... bindings) {
    return new DbUtil(connection, sql, bindings);
  }

  public <T> List<T> fetch(@NonNull Function<? super Record, T> mapper) {
    if (Objects.isNull(bindings)) {
      return Try.success(connection)
        .mapTry(DSL::using)
        .flatMapTry(dslContext -> Try.success(dslContext).mapTry(context -> context.fetch(sql)).andThenTry(dslContext::close))
        .andThenTry(connection::close)
        .mapTry(result -> result.map(mapper::apply))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(ImmutableList.of());
    } else {
      return Try.success(connection)
        .mapTry(DSL::using)
        .flatMapTry(dslContext -> Try.success(dslContext).mapTry(context -> context.fetch(sql, bindings)).andThenTry(dslContext::close))
        .andThenTry(connection::close)
        .mapTry(result -> result.map(mapper::apply))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(ImmutableList.of());
    }
  }

  public <T> Optional<T> fetchSingle(@NonNull Function<? super Record, T> mapper) {
    if (Objects.isNull(bindings)) {
      return Try.success(connection)
        .mapTry(DSL::using)
        .flatMapTry(dslContext -> Try.success(dslContext).mapTry(context -> context.fetchOptional(sql)).andThenTry(dslContext::close))
        .andThenTry(connection::close)
        .mapTry(r -> r.map(mapper))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(Optional.empty());
    } else {
      return Try.success(connection)
        .mapTry(DSL::using)
        .flatMapTry(dslContext -> Try.success(dslContext).mapTry(context -> context.fetchOptional(sql, bindings)).andThenTry(dslContext::close))
        .andThenTry(connection::close)
        .mapTry(r -> r.map(mapper))
        .onFailure(t -> log.error("fetch records error: {}", t))
        .getOrElse(Optional.empty());
    }
  }

  public int execute() {
    if (Objects.isNull(bindings)) {
      return Try.success(connection)
        .mapTry(DSL::using)
        .flatMapTry(dslContext -> Try.success(dslContext).mapTry(context -> context.execute(sql)).andThenTry(dslContext::close))
        .andThenTry(connection::close)
        .onFailure(t -> log.error("execute sql error: {}", t))
        .getOrElse(-1);
    } else {
      return Try.success(connection)
        .mapTry(DSL::using)
        .flatMapTry(dslContext -> Try.success(dslContext).mapTry(context -> context.execute(sql, bindings)).andThenTry(dslContext::close))
        .andThenTry(connection::close)
        .onFailure(t -> log.error("execute sql error: {}", t))
        .getOrElse(-1);
    }
  }
}

