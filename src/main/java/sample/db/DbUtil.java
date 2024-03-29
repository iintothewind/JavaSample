package sample.db;

import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.TransactionalCallable;
import org.jooq.TransactionalRunnable;
import org.jooq.impl.DSL;

/**
 * to support mysql 5.7, jooq 3.9.6 is required
 */
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
   *                   Not resources that you passed to the DSLContext.</b> NOTE: You have to manually <b>close</b> the connection after using DSLContext
   * @param sql        statement, better to be prepared statement
   */
  public static DbUtil withSql(@NonNull final Connection connection, @NonNull final String sql) {
    return new DbUtil(connection, sql, null);
  }

  public static <T> T callTrx(@NonNull final Connection connection, @NonNull final TransactionalCallable<T> callable) {
    final T t = Try.of(() -> DSL.using(connection))
        .mapTry(dslContext -> callable.run(dslContext.configuration()))
        .andFinallyTry(connection::close)
        .onFailure(e -> log.error("failed to callTransaction: ", e))
        .getOrNull();
    return t;
  }

  public static <T> T callTrx(@NonNull final TransactionalCallable<T> callable) {
    return callTrx(DataSourceUtil.getConnection(), callable);
  }

  public static void execTrx(@NonNull final Connection connection, @NonNull final TransactionalRunnable runnable) {
    Try.run(() -> runnable.run(DSL.using(connection).configuration()))
        .andFinallyTry(connection::close)
        .onFailure(e -> log.error("failed to execTransaction: ", e));
  }

  public static void execTrx(@NonNull final TransactionalRunnable runnable) {
    execTrx(DataSourceUtil.getConnection(), runnable);
  }

  /**
   * fetch with default db connection
   *
   * @param sql statement, better to be prepared statement
   */
  public static DbUtil withSql(@NonNull final String sql) {
    return withSql(DataSourceUtil.getConnection(), sql);
  }


  public DbUtil withBindings(@NonNull final Object... bindings) {
    return new DbUtil(connection, sql, bindings);
  }


  /**
   * execute sql to fetch result as a list use mapper to convert Record to T type Empty list will be returned if any exception occurs
   * <br>
   * <b>Caution:
   * To ensure function robust, All exceptions will be contained in Try monad, means code never breaks in this function when sql error, db connection error, etc happen</b>
   * <br>
   * <b>Check your logs for any possible errors, handle empty value return</b>
   *
   * @return the list of T which is converted from fetched records, empty when any exception occurs
   */
  public <T> List<T> fetch(@NonNull Function<? super Record, T> mapper) {
    return Try
        .of(() -> DSL.using(connection))
        .mapTry(dslContext -> Try.of(() -> bindings).filter(Objects::nonNull).mapTry(objs -> dslContext.fetch(sql, objs)).getOrElse(() -> dslContext.fetch(sql)))
        .andFinallyTry(connection::close)
        .mapTry(result -> result.map(mapper::apply))
        .onFailure(t -> log.error("failed to fetch records with sql : {}", sql, t))
        .getOrElse(ImmutableList.of());
  }

  /**
   * execute sql to fetch result as an Optional use mapper to convert Record to T type Optional.empty() will be returned if any exception occurs
   * <br>
   * <b>Caution:
   * To ensure function robust, All exceptions will be contained in Try monad, means code never breaks in this function when sql error, db connection error, etc happen</b>
   * <br>
   * <b>Check your logs for any possible errors, handle empty value return</b>
   *
   * @return the Optional of T which is converted from fetched records, empty when any exception occurs
   */
  public <T> Optional<T> fetchSingle(@NonNull Function<? super Record, T> mapper) {
    return Try
        .of(() -> DSL.using(connection))
        .mapTry(dslContext -> Try.of(() -> bindings).filter(Objects::nonNull).mapTry(objs -> dslContext.fetchSingle(sql, objs)).getOrElse(() -> dslContext.fetchSingle(sql)))
        .andFinallyTry(connection::close)
        .mapTry(mapper::apply)
        .onFailure(t -> log.error("failed to fetch records with sql: {}", sql, t))
        .toJavaOptional();
  }

  /**
   * <b>Caution:
   * To ensure function robust, All exceptions will be contained in Try monad, Means code never breaks in this function when sql error, db connection error, etc happen</b>
   * <br>
   * <b>Check your logs for any possible errors, handle empty value return</b>
   *
   * @return number of rows affected, -1 when any exception occurs
   */
  public void execute() {
    execTrx(connection, cfg -> Try
        .success(bindings)
        .filter(Objects::nonNull)
        .map(b -> DSL.using(cfg).execute(sql, b))
        .getOrElse(() -> DSL.using(cfg).execute(sql)));
  }
}

