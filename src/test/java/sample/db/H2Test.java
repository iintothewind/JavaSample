package sample.db;

import com.zaxxer.hikari.HikariDataSource;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;


@Slf4j
public class H2Test {

    private HikariDataSource dataSource = null;

    @BeforeEach
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
        final List<Tuple2<String, String>> lst = DbUtil
                .withSql(DataSourceUtil.getConnection(dataSource), "SELECT * FROM INFORMATION_SCHEMA.settings")
                .fetch(record -> Tuple.of(record.getValue("SETTING_NAME", String.class), record.getValue("SETTING_VALUE", String.class)));
        lst.forEach(t -> log.info("settingName: {}, settingValue: {}", t._1, t._2));
    }


}
