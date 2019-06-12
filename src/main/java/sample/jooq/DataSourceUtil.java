package sample.jooq;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import io.vavr.collection.HashMap;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Objects;
import java.util.Optional;


@Slf4j
public class DataSourceUtil {
  private final DataSource dataSource;

  static {
    if (Objects.isNull(System.getProperty("JAWS_HOSTNAME"))) {
      System.setProperty("JAWS_HOSTNAME", "msmaster.qa.paypal.com");
      System.setProperty("JAWS_SSH_RESTRICTED_STAGE", "true");
    }
  }

  private DataSourceUtil(final String url, final String userName, final String password) {
    dataSource = Try
      .of(() -> DruidDataSourceFactory.createDataSource(HashMap
        .<String, String>empty()
        .put("url", Optional.ofNullable(url).orElse("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL"))
        .put("username", Optional.ofNullable(userName).orElse("postgress"))
        .put("password", Optional.ofNullable(password).orElse("admin"))
        .put("maxActive", "2")
        .put("initialSize", "1")
        .put("queryTimeout", "60")
        .put("transactionQueryTimeout", "60")
        .toJavaMap()))
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
    private static final DataSourceUtil instance = new DataSourceUtil("", "", "");
  }
}
