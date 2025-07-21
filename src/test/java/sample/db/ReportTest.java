package sample.db;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;


public class ReportTest {
    private HikariDataSource dataSource = null;


    @BeforeEach
    public void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/echobase?useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("admin");
    }


}
