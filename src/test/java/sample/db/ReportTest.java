package sample.db;

import org.junit.Before;
import com.zaxxer.hikari.HikariDataSource;


public class ReportTest {
    private HikariDataSource dataSource = null;


    @Before
    public void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/echobase?useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("admin");
    }


}
