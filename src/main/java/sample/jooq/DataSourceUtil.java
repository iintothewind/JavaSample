package sample.jooq;

import com.zaxxer.hikari.HikariDataSource;
import io.vavr.collection.HashMap;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;


@Slf4j
public class DataSourceUtil {
  private final DataSource dataSource;

  public DataSourceUtil(final String url, final String userName, final String password) {
    dataSource = Try.of(() -> {
              final HikariDataSource hikari = new HikariDataSource();
              hikari.setJdbcUrl(Optional.ofNullable(url).orElse("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL"));
              hikari.setUsername(Optional.ofNullable(userName).orElse("postgres"));
              hikari.setPassword(Optional.ofNullable(password).orElse("admin"));
              hikari.setMaximumPoolSize(5);
              return hikari;
            })
            .onFailure(t -> log.error("DbUtil initialization error: {}", t.getMessage(), t))
            .getOrElseThrow(() -> new IllegalStateException("DbUtil initialization failed"));
  }

  public DataSourceUtil() {
    this(null, null, null);
  }

  public static Connection getConnection() {
    return Try
      .of(InstanceHolder.instance.dataSource::getConnection)
      .onFailure(Throwable::printStackTrace)
      .getOrElseThrow(() -> new IllegalStateException("connect to db error"));
  }

  private static class InstanceHolder {
    private static final DataSourceUtil instance = new DataSourceUtil();
  }
}
