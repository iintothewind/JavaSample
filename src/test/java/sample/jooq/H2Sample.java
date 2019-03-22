package sample.jooq;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.sql.DataSource;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Test;
import org.springframework.lang.NonNull;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;


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

    public <T> List<T> fetch(@NonNull final String sql, @NonNull RecordMapper<? super Record, T> mapper) {
        return Try.of(() -> DSL.using(dataSource.getConnection()))
            .mapTry(dslContext -> dslContext.fetch(sql))
            .mapTry(result -> result.map(mapper))
            .getOrElse(ImmutableList.of());
    }

    public <T> Optional<T> fetchSingle(@NonNull final String sql, @NonNull Function<? super Record, T> mapper) {
        return Try.of(() -> DSL.using(dataSource.getConnection()))
            .mapTry(dslContext -> dslContext.fetchOptional(sql))
            .mapTry(r -> r.map(mapper))
            .getOrElse(Optional.empty());
    }

    @Test
    public void testFetch() throws SQLException {
        DSL.using(dataSource.getConnection())
            .select(DSL.field("ID"), DSL.field("TOPIC"))
            .from(" INFORMATION_SCHEMA.HELP ")
            .where("id = ? and topic = ? ", 0, "SELECT")
            .fetch()
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
