package sample.vertx;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

public class JdbcSample {
  private DataSource dataSource = null;
  private SQLClient sqlClient = null;

  @Before
  public void setUp() throws Exception {
    dataSource = DruidDataSourceFactory.createDataSource(ImmutableMap.of(
      "url", "jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL",
      "username", "postgress",
      "password", "admin",
      "validationQuery", "select 1"
    ));
    sqlClient = JDBCClient.create(Vertx.vertx(), dataSource);
  }

  @After
  public void tearDown() throws InterruptedException {
    TimeUnit.SECONDS.sleep(5);

  }

  @Test
  public void testSelect() {
    sqlClient.query("SELECT * FROM INFORMATION_SCHEMA.HELP ", event -> {
      if (event.succeeded()) {
        event
          .result()
          .getRows()
          .stream()
          .map(o -> Tuple.of(o.getInteger("ID"), o.getString("TOPIC")))
          .forEach(System.out::println);
      } else {
        event.cause().printStackTrace();
      }
    });
  }

}
