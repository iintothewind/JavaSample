package sample.jooq;

import java.sql.Connection;
import javax.sql.DataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.ImmutableMap;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DataSourceUtil {
  private final DataSource dataSource;

  private DataSourceUtil() {
    dataSource = Try
      .of(() -> DruidDataSourceFactory.createDataSource(ImmutableMap.of(
        "url", "jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL",
        "username", "postgress",
        "password", "admin"
      )))
      .onFailure(throwable -> log.error("DbUtil initialization error: {}", throwable))
      .getOrElseThrow(() -> new IllegalStateException("DbUtil initialization failed"));
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
