package sample.db;

import com.zaxxer.hikari.HikariDataSource;
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
                    hikari.setJdbcUrl(Optional.ofNullable(url).orElse("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;DB_CLOSE_ON_EXIT=FALSE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;MODE=MySQL"));
                    hikari.setUsername(Optional.ofNullable(userName).orElse("admin"));
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

    public static DataSource getDataSource() {
        return InstanceHolder.instance.dataSource;
    }

    public static Connection getConnection() {
        return Try
                .of(InstanceHolder.instance.dataSource::getConnection)
                .onFailure(Throwable::printStackTrace)
                .getOrElseThrow(() -> new IllegalStateException("connect to db error"));
    }

    public static Connection getConnection(final DataSource dataSource) {
        return Try.of(() -> dataSource.getConnection()).getOrElseThrow(e -> new IllegalStateException("failed to getConnection: ", e));
    }

    private static class InstanceHolder {
        private static final DataSourceUtil instance = new DataSourceUtil();
    }
}
