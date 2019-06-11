package sample.jooq;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Optional;


public class H2Sample {
  private DataSource dataSource = null;

  @Before
  public void setUp() throws Exception {
    dataSource = DruidDataSourceFactory.createDataSource(ImmutableMap.of(
      "url", "jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL",
      "username", "postgress",
      "password", "admin",
      "validationQuery", "select 1"
    ));
  }

  @Test
  public void testDbUtil() {
    final Optional<Tuple2<Integer, String>> tuple = DbUtil
      .withSql(DataSourceUtil.getConnection(), "SELECT * FROM INFORMATION_SCHEMA.HELP where id = ? ")
      .withBindings(0)
      .fetchSingle(record -> Tuple.of(record.getValue("ID", Integer.class), record.getValue("TOPIC", String.class)));
    tuple.ifPresent(System.out::println);
  }

}
