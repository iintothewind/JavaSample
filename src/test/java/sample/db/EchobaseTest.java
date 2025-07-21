package sample.db;

import com.zaxxer.hikari.HikariDataSource;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Supplier;


public class EchobaseTest {
    private HikariDataSource dataSource = null;

    @BeforeEach
    public void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://192.168.1.68:3306/echobase?useSSL=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&allowPublicKeyRetrieval=true");
        dataSource.setUsername("czhang");
        dataSource.setPassword("chzh123$");
    }

    @Test
    public void testDbUtil() {
        final Optional<Tuple2<Integer, String>> tuple = DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),
                "SELECT * FROM users where id = ? ")
            .withBindings(1)
            .fetchSingle(record -> Tuple.of(record.getValue("id", Integer.class), record.getValue("userType", String.class)));
        tuple.ifPresent(System.out::println);

    }

}
