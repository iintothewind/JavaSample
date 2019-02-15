package sample.jooq;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

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
  public void testFetch() throws SQLException {
    DSL
      .using(dataSource.getConnection())
      .fetch("SELECT * FROM INFORMATION_SCHEMA.HELP ")
      .map(record -> Tuple.of(record.getValue("ID", Integer.class), record.getValue("TOPIC", String.class)))
      .forEach(System.out::println);
  }

  @Test
  public void testFetchSingle() throws SQLException {
    Tuple2<Integer, String> tuple = DSL
      .using(dataSource.getConnection())
      .fetchOptional("SELECT * FROM INFORMATION_SCHEMA.HELP where id = '0' ")
      .map(record -> Tuple.of(record.getValue("ID", Integer.class), record.getValue("TOPIC", String.class)))
      .orElse(Tuple.of(0, "None"));

    System.out.println(tuple);
  }

}
