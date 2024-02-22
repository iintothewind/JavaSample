package sample.jooq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariDataSource;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.control.Try;
import lombok.*;
import org.junit.Before;
import org.junit.Test;
import sample.http.JsonUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CompletedOrdersTest {
    private HikariDataSource dataSource = null;

    @Before
    public void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/echobase?useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("admin");
    }

    @Builder
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DriverSettlementSnapshot {
        @With
        private Integer driverId;
        @With
        private Integer total;
        @With
        private Integer completed;
        @With
        private Integer otpEffective;
        @With
        private BigDecimal otp;
        @With
        private Integer greenZoneParcel;
        @With
        private Integer yellowZoneParcel;
        @With
        private BigDecimal exceededWeight;
    }

    @Test
    public void testLoadRoutes() {
        final String sql = "SELECT r.readableName, r.routeCloseDate, r.settlementSnapshot  from routes r where r.routePlanner = 194";
        final List<Tuple3<String, LocalDateTime, String>> routes = DbUtil
                .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),
                        sql)
                .fetch(record -> Tuple.of(record.getValue("readableName", String.class), record.getValue("routeCloseDate", LocalDateTime.class), record.getValue("settlementSnapshot", String.class)));
        final List<Tuple3<String, LocalDateTime, String>> driverRoutes = routes.stream()
                .filter(t -> Optional
                        .ofNullable(JsonUtil.load(t._3, new TypeReference<List<DriverSettlementSnapshot>>() {
                        }))
                        .orElse(ImmutableList.of())
                        .stream().anyMatch(s -> s.getDriverId() == 1834))
                .collect(Collectors.toList());
        System.out.println(driverRoutes);
        final List<DriverSettlementSnapshot> snapshots = routes.stream()
                .flatMap(t -> Optional.ofNullable(JsonUtil.load(t._3, new TypeReference<List<DriverSettlementSnapshot>>() {
                        })).orElse(ImmutableList.of())
                        .stream())
                .toList();
        System.out.println(snapshots.stream().filter(s -> s.getDriverId() == 1834).collect(Collectors.toList()));
        final int total = snapshots.stream().mapToInt(DriverSettlementSnapshot::getCompleted).sum();
        System.out.println(total);
        final Map<Integer, Integer> driverCompletes = snapshots.stream().filter(s -> s.getCompleted() > 0).collect(Collectors.toMap(DriverSettlementSnapshot::getDriverId, DriverSettlementSnapshot::getCompleted, Integer::sum));
        System.out.println(driverCompletes);
    }
}
