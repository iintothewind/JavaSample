package sample.jooq;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariDataSource;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.function.Supplier;


public class H2Sample {
  private HikariDataSource dataSource = null;

  @Before
  public void setUp() throws Exception {
//    dataSource = DruidDataSourceFactory.createDataSource(ImmutableMap.of(
//      "url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL",
//      "username", "postgress",
//      "password", "admin",
//      "validationQuery", "select 1"
//    ));
    dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL");
    dataSource.setUsername("postgress");
    dataSource.setPassword("admin");
  }

  @Test
  public void testDbUtil() {
    final Optional<Tuple2<Integer, String>> tuple = DbUtil
      .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new), "SELECT * FROM INFORMATION_SCHEMA.HELP where id = ? ")
      .withBindings(0)
      .fetchSingle(record -> Tuple.of(record.getValue("ID", Integer.class), record.getValue("TOPIC", String.class)));
    tuple.ifPresent(System.out::println);
  }

}
