package sample.jooq;


import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import org.junit.Test;

import java.util.Optional;

@Slf4j
public class JdbiTest {
    @Test
    public void testQuery01() {
        Optional<Tuple2<String, String>> result = Jdbi
                .create(DataSourceUtil.getDataSource())
                .withHandle(handle -> handle
                        .createQuery("SELECT * FROM INFORMATION_SCHEMA.settings s where s.SETTING_NAME = :name")
                        .bind("name", "MODE")
                        .map(r -> Tuple.of(r.getColumn("SETTING_NAME", String.class), r.getColumn("SETTING_VALUE", String.class)))
                        .findOne());

        System.out.println(result);

    }

    @Test
    public void testQuery02() {
        Optional<Tuple2<String, String>> result = Jdbi
                .create(DataSourceUtil.getDataSource())
                .withHandle(handle -> handle
                        .setSqlLogger(new Slf4JSqlLogger(log))
                        .createQuery("SELECT * FROM INFORMATION_SCHEMA.collations c where c.COLLATION_CATALOG = :catalog and c.LANGUAGE_TAG is not null")
                        .bind("catalog", "TEST")
                        .map(r -> Tuple.of(r.getColumn("COLLATION_NAME", String.class), r.getColumn("LANGUAGE_TAG", String.class)))
                        .findFirst());

        System.out.println(result);

    }
}
