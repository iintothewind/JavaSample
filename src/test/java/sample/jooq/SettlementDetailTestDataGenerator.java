package sample.jooq;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.collection.HashMap;
import io.vavr.control.Try;


public class SettlementDetailTestDataGenerator {
    private HikariDataSource dataSource = null;
    private String driverSql = "INSERT INTO settlement_details values (null, '%s', '%s', '%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',null,null,null)";
    private String fleetSql = "INSERT INTO settlement_details values (null, '%s', '%s', '%s', null,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',null,null,null)";
    private String deliveryCompanySql = "INSERT INTO settlement_details values (null, '%s', '%s', null, null,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',null,null,null)";
    private String clientSql = "INSERT INTO settlement_details values (null, '%s', null, null, null,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',null,null,null)";

    @Before
    public void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/echobase?useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("password");
    }

    private List<Tuple2<Integer, String>> loadClients() {
        final List<Tuple2<Integer, String>> clients = DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),
                "SELECT * FROM clients")
            .fetch(record -> Tuple.of(record.getValue("id", Integer.class), record.getValue("clientName", String.class)));
        return clients;
    }

    private List<Tuple2<Integer, Integer>> loadDeliveryCompanies(final Integer clientId) {
        final List<Tuple2<Integer, Integer>> dcs = DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),
                "select dc.* from delivery_companies dc join client_delivery_companies cdc on dc.id = cdc.deliveryCompany where cdc.client = ?")
            .withBindings(clientId)
            .fetch(record -> Tuple.of(record.getValue("id", Integer.class), record.getValue("manager", Integer.class)));
        return dcs;
    }

    private List<Tuple2<Integer, String>> loadFleets(final Integer deliveryCompanyId) {
        final List<Tuple2<Integer, String>> fleets = DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),
                "select f.* from fleets f join delivery_company_fleets dcf on f.id = dcf.fleet where dcf.deliveryCompany = ?")
            .withBindings(deliveryCompanyId)
            .fetch(record -> Tuple.of(record.getValue("id", Integer.class), record.getValue("fleetName", String.class)));
        return fleets;
    }

    private List<Tuple2<Integer, String>> loadDrivers(final Integer fleetId) {
        final List<Tuple2<Integer, String>> drivers = DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),
                "select u.id,u.email,u.userType from users u join fleet_drivers fd on u.id = fd.userId where fd.fleetId = ?")
            .withBindings(fleetId)
            .fetch(record -> Tuple.of(record.getValue("id", Integer.class), record.getValue("email", String.class)));
        return drivers;
    }

    private List<String> genDates(final LocalDate from, final LocalDate to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkArgument(to.isAfter(from));
        final long n = ChronoUnit.DAYS.between(from, to);
        final List<String> dates = LongStream.range(0L, n+1)
            .mapToObj(from::plusDays)
            .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .collect(Collectors.toList());
        return dates;
    }

    private List<Map<String, Integer>> genDrivers() {
        final int zone0Rate = 3;
        final int zone1Rate = 4;
        final int zone2Rate = 5;

        final int zone0Parcel = rand(30, 50);
        final int zone0Payment = zone0Parcel * zone0Rate;
        final int zone1Parcel = rand(10, 20);
        final int zone1Payment = zone1Parcel * zone1Rate;
        final int zone2Parcel = rand(1, 10);
        final int zone2Payment = zone2Parcel * zone2Rate;
        final int totalParcel = zone0Parcel + zone1Parcel + zone2Parcel;
        final int overWeight = rand(1, 50);
        final int routeIncentiveParcel = rand(1, 30);
        final int green = rand(10, 40);
        final int yellow = rand(1, 20);
        final int timeIncentive = (int) (green * 0.15d);
        final int weightIncentive = (int) (overWeight * 0.15d);
        final int routeIncentive = (int) (routeIncentiveParcel * 0.15d);
        final int totalPayment = zone0Payment + zone1Payment + zone2Payment + timeIncentive + weightIncentive + routeIncentive;
        final int tax = (int) (totalPayment * 0.15d);
        final int totalPaid = (int) ((totalPayment - tax) * 0.75d);

        final List<Map<String, Integer>> drivers = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .flatMap(f -> loadDrivers(f._3).stream().map(d -> Tuple.of(f._1, f._2, f._3, d._1)))
            .map(d -> HashMap.<String, Integer>empty()
                .put("clientId", d._1)
                .put("deliveryCompanyId", d._2)
                .put("fleetId", d._3)
                .put("driverId", d._4)
                .put("totalParcel", totalParcel)
                .put("zone0Parcel", zone0Parcel)
                .put("zone1Parcel", zone1Parcel)
                .put("zone2Parcel", zone2Parcel)
                .put("totalPayment", totalPayment)
                .put("tax", tax)
                .put("totalPaid", totalPaid)
                .put("zone0", zone0Payment)
                .put("zone1", zone1Payment)
                .put("zone2", zone2Payment)
                .put("overWeight", overWeight)
                .put("routeIncentiveParcel", routeIncentiveParcel)
                .put("greenZoneParcel", green)
                .put("yellowZoneParcel", yellow)
                .put("timeIncentive", timeIncentive)
                .put("weightIncentive", weightIncentive)
                .put("routeIncentive", routeIncentive)
                .toJavaMap())
            .collect(Collectors.toList());

        return drivers;
    }

    private List<Map<String, Integer>> genFleets(final List<Map<String, Integer>> drivers) {
        final List<Map<String, Integer>> fleets = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .map(f -> Tuple.of(f, drivers.stream().filter(m -> Objects.equals(m.getOrDefault("fleetId", 0), f._3)).collect(Collectors.toList())))
            .map(t -> HashMap.<String, Integer>empty()
                .put("clientId", t._1._1)
                .put("deliveryCompanyId", t._1._2)
                .put("fleetId", t._1._3)
                .put("driverId", 0)
                .put("totalParcel", t._2.stream().mapToInt(m -> m.getOrDefault("totalParcel", 0)).sum())
                .put("zone0Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone0Parcel", 0)).sum())
                .put("zone1Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone1Parcel", 0)).sum())
                .put("zone2Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone2Parcel", 0)).sum())
                .put("totalPayment", t._2.stream().mapToInt(m -> m.getOrDefault("totalPayment", 0)).sum())
                .put("tax", t._2.stream().mapToInt(m -> m.getOrDefault("tax", 0)).sum())
                .put("totalPaid", t._2.stream().mapToInt(m -> m.getOrDefault("totalPaid", 0)).sum())
                .put("zone0", t._2.stream().mapToInt(m -> m.getOrDefault("zone0", 0)).sum())
                .put("zone1", t._2.stream().mapToInt(m -> m.getOrDefault("zone1", 0)).sum())
                .put("zone2", t._2.stream().mapToInt(m -> m.getOrDefault("zone2", 0)).sum())
                .put("overWeight", t._2.stream().mapToInt(m -> m.getOrDefault("overWeight", 0)).sum())
                .put("routeIncentiveParcel", t._2.stream().mapToInt(m -> m.getOrDefault("routeIncentiveParcel", 0)).sum())
                .put("greenZoneParcel", t._2.stream().mapToInt(m -> m.getOrDefault("greenZoneParcel", 0)).sum())
                .put("yellowZoneParcel", t._2.stream().mapToInt(m -> m.getOrDefault("yellowZoneParcel", 0)).sum())
                .put("timeIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("timeIncentive", 0)).sum())
                .put("weightIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("weightIncentive", 0)).sum())
                .put("routeIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("routeIncentive", 0)).sum())
                .toJavaMap())
            .collect(Collectors.toList());
        return fleets;
    }

    private List<Map<String, Integer>> genDriverCompanies(final List<Map<String, Integer>> fleets) {
        final List<Map<String, Integer>> deliveryCompanies = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .map(dc -> Tuple.of(dc, fleets.stream().filter(f -> Objects.equals(f.getOrDefault("deliveryCompanyId", 0), dc._2)).collect(Collectors.toList())))
            .map(t -> HashMap.<String, Integer>empty()
                .put("clientId", t._1._1)
                .put("deliveryCompanyId", t._1._2)
                .put("fleetId", 0)
                .put("driverId", 0)
                .put("totalParcel", t._2.stream().mapToInt(m -> m.getOrDefault("totalParcel", 0)).sum())
                .put("zone0Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone0Parcel", 0)).sum())
                .put("zone1Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone1Parcel", 0)).sum())
                .put("zone2Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone2Parcel", 0)).sum())
                .put("totalPayment", t._2.stream().mapToInt(m -> m.getOrDefault("totalPayment", 0)).sum())
                .put("tax", t._2.stream().mapToInt(m -> m.getOrDefault("tax", 0)).sum())
                .put("totalPaid", t._2.stream().mapToInt(m -> m.getOrDefault("totalPaid", 0)).sum())
                .put("zone0", t._2.stream().mapToInt(m -> m.getOrDefault("zone0", 0)).sum())
                .put("zone1", t._2.stream().mapToInt(m -> m.getOrDefault("zone1", 0)).sum())
                .put("zone2", t._2.stream().mapToInt(m -> m.getOrDefault("zone2", 0)).sum())
                .put("overWeight", t._2.stream().mapToInt(m -> m.getOrDefault("overWeight", 0)).sum())
                .put("routeIncentiveParcel", t._2.stream().mapToInt(m -> m.getOrDefault("routeIncentiveParcel", 0)).sum())
                .put("greenZoneParcel", t._2.stream().mapToInt(m -> m.getOrDefault("greenZoneParcel", 0)).sum())
                .put("yellowZoneParcel", t._2.stream().mapToInt(m -> m.getOrDefault("yellowZoneParcel", 0)).sum())
                .put("timeIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("timeIncentive", 0)).sum())
                .put("weightIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("weightIncentive", 0)).sum())
                .put("routeIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("routeIncentive", 0)).sum())
                .toJavaMap())
            .collect(Collectors.toList());
        return deliveryCompanies;
    }

    private List<Map<String, Integer>> genClients(final List<Map<String, Integer>> driverCompanies) {
        final List<Map<String, Integer>> clients = loadClients().stream()
            .map(c -> Tuple.of(c, driverCompanies.stream().filter(dc -> Objects.equals(dc.getOrDefault("clientId", 0), c._1)).collect(Collectors.toList())))
            .map(t -> HashMap.<String, Integer>empty()
                .put("clientId", t._1._1)
                .put("deliveryCompanyId", 0)
                .put("fleetId", 0)
                .put("driverId", 0)
                .put("totalParcel", t._2.stream().mapToInt(m -> m.getOrDefault("totalParcel", 0)).sum())
                .put("zone0Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone0Parcel", 0)).sum())
                .put("zone1Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone1Parcel", 0)).sum())
                .put("zone2Parcel", t._2.stream().mapToInt(m -> m.getOrDefault("zone2Parcel", 0)).sum())
                .put("totalPayment", t._2.stream().mapToInt(m -> m.getOrDefault("totalPayment", 0)).sum())
                .put("tax", t._2.stream().mapToInt(m -> m.getOrDefault("tax", 0)).sum())
                .put("totalPaid", t._2.stream().mapToInt(m -> m.getOrDefault("totalPaid", 0)).sum())
                .put("zone0", t._2.stream().mapToInt(m -> m.getOrDefault("zone0", 0)).sum())
                .put("zone1", t._2.stream().mapToInt(m -> m.getOrDefault("zone1", 0)).sum())
                .put("zone2", t._2.stream().mapToInt(m -> m.getOrDefault("zone2", 0)).sum())
                .put("overWeight", t._2.stream().mapToInt(m -> m.getOrDefault("overWeight", 0)).sum())
                .put("routeIncentiveParcel", t._2.stream().mapToInt(m -> m.getOrDefault("routeIncentiveParcel", 0)).sum())
                .put("greenZoneParcel", t._2.stream().mapToInt(m -> m.getOrDefault("greenZoneParcel", 0)).sum())
                .put("yellowZoneParcel", t._2.stream().mapToInt(m -> m.getOrDefault("yellowZoneParcel", 0)).sum())
                .put("timeIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("timeIncentive", 0)).sum())
                .put("weightIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("weightIncentive", 0)).sum())
                .put("routeIncentive", t._2.stream().mapToInt(m -> m.getOrDefault("routeIncentive", 0)).sum())
                .toJavaMap())
            .collect(Collectors.toList());
        return driverCompanies;
    }

    private List<String> genSqls(final String settlementDate) {
        final List<Map<String, Integer>> drivers = genDrivers();
        final List<Map<String, Integer>> fleets = genFleets(drivers);
        final List<Map<String, Integer>> deliveryCompanies = genFleets(fleets);
        final List<Map<String, Integer>> clients = genFleets(deliveryCompanies);

        final List<String> driverSqls = drivers.stream().map(d -> String.format(driverSql,
                d.get("clientId"),
                d.get("deliveryCompanyId"),
                d.get("fleetId"),
                d.get("driverId"),
                d.get("totalParcel"),
                d.get("zone0Parcel"),
                d.get("zone1Parcel"),
                d.get("zone2Parcel"),
                d.get("totalPayment"),
                d.get("tax"),
                d.get("totalPaid"),
                d.get("zone0"),
                d.get("zone1"),
                d.get("zone2"),
                d.get("overWeight"),
                d.get("routeIncentiveParcel"),
                d.get("greenZoneParcel"),
                d.get("yellowZoneParcel"),
                d.get("timeIncentive"),
                d.get("weightIncentive"),
                d.get("routeIncentive")
                , settlementDate))
            .collect(Collectors.toList());

        final List<String> fleetSqls = fleets.stream().map(f -> String.format(fleetSql,
                f.get("clientId"),
                f.get("deliveryCompanyId"),
                f.get("fleetId"),
                f.get("totalParcel"),
                f.get("zone0Parcel"),
                f.get("zone1Parcel"),
                f.get("zone2Parcel"),
                f.get("totalPayment"),
                f.get("tax"),
                f.get("totalPaid"),
                f.get("zone0"),
                f.get("zone1"),
                f.get("zone2"),
                f.get("overWeight"),
                f.get("routeIncentiveParcel"),
                f.get("greenZoneParcel"),
                f.get("yellowZoneParcel"),
                f.get("timeIncentive"),
                f.get("weightIncentive"),
                f.get("routeIncentive")
                , settlementDate))
            .collect(Collectors.toList());

        final List<String> deliveryCompanySqls = deliveryCompanies.stream().map(f -> String.format(deliveryCompanySql,
                f.get("clientId"),
                f.get("deliveryCompanyId"),
                f.get("totalParcel"),
                f.get("zone0Parcel"),
                f.get("zone1Parcel"),
                f.get("zone2Parcel"),
                f.get("totalPayment"),
                f.get("tax"),
                f.get("totalPaid"),
                f.get("zone0"),
                f.get("zone1"),
                f.get("zone2"),
                f.get("overWeight"),
                f.get("routeIncentiveParcel"),
                f.get("greenZoneParcel"),
                f.get("yellowZoneParcel"),
                f.get("timeIncentive"),
                f.get("weightIncentive"),
                f.get("routeIncentive")
                , settlementDate))
            .collect(Collectors.toList());

        final List<String> clientSqls = clients.stream().map(f -> String.format(clientSql,
                f.get("clientId"),
                f.get("totalParcel"),
                f.get("zone0Parcel"),
                f.get("zone1Parcel"),
                f.get("zone2Parcel"),
                f.get("totalPayment"),
                f.get("tax"),
                f.get("totalPaid"),
                f.get("zone0"),
                f.get("zone1"),
                f.get("zone2"),
                f.get("overWeight"),
                f.get("routeIncentiveParcel"),
                f.get("greenZoneParcel"),
                f.get("yellowZoneParcel"),
                f.get("timeIncentive"),
                f.get("weightIncentive"),
                f.get("routeIncentive")
                , settlementDate))
            .collect(Collectors.toList());

        return Stream.of(driverSqls, fleetSqls, deliveryCompanySqls, clientSqls).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private int rand(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Test
    public void testLoadClients() {
        final List<Tuple2<Integer, String>> clients = loadClients();
        System.out.println(clients);
    }

    @Test
    public void testLoadDeliverCompanies() {
        final List<Tuple2<Integer, Integer>> dcs = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1))).collect(Collectors.toList());
        System.out.println(dcs);
    }

    @Test
    public void testLoadFleets() {
        final List<Tuple3<Integer, Integer, Integer>> fleets = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .collect(Collectors.toList());
        System.out.println(fleets);
    }

    @Test
    public void testLoadDrivers() {
        final List<Tuple4<Integer, Integer, Integer, Integer>> drivers = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .flatMap(f -> loadDrivers(f._3).stream().map(d -> Tuple.of(f._1, f._2, f._3, d._1)))
            .collect(Collectors.toList());
        System.out.println(drivers);
    }

    @Test
    public void testLoadDriverMatrix() {
        final List<Tuple2<Integer, String>> clients = loadClients();
        final List<Tuple4<Integer, Integer, Integer, Integer>> drivers = clients.stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .flatMap(f -> loadDrivers(f._3).stream().map(d -> Tuple.of(f._1, f._2, f._3, d._1)))
            .collect(Collectors.toList());
        System.out.println(drivers);
    }

    @Test
    public void testGenDates() {
        final List<String> dates = genDates(LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 5));
        System.out.println(dates);
    }

    @Test
    public void testGenDriverSettlement() {
        int zone0Rate = 3;
        int zone1Rate = 4;
        int zone2Rate = 5;

        int zone0 = rand(30, 50);
        int zone0Payment = zone0 * zone0Rate;
        int zone1 = rand(10, 20);
        int zone1Payment = zone1 * zone1Rate;
        int zone2 = rand(1, 10);
        int zone2Payment = zone2 * zone2Rate;
        int total = zone0 + zone1 + zone2;
        int overWeight = rand(1, 50);
        int routeIncentiveParcel = rand(1, 30);
        int green = rand(10, 40);
        int yellow = rand(1, 20);
        int timeIncentive = (int) (green * 0.15d);
        int weightIncentive = (int) (overWeight * 0.15d);
        int routeIncentive = (int) (routeIncentiveParcel * 0.15d);
        int income = zone0Payment + zone1Payment + zone2Payment + timeIncentive + weightIncentive + routeIncentive;
        int tax = (int) (income * 0.15d);
        int payment = (int) ((income - tax) * 0.75d);
        final List<Tuple4<Integer, Integer, Integer, Integer>> drivers = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .flatMap(f -> loadDrivers(f._3).stream().map(d -> Tuple.of(f._1, f._2, f._3, d._1)))
            .collect(Collectors.toList());

        String sql = "INSERT INTO settlement_details values (null, '%s', '%s', '%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',null,null,null)";

        final List<String> sqls = drivers
            .stream()
            .map(d -> String.format(sql, d._1, d._2, d._3(), d._4, total, zone0, zone1, zone2, income, tax, payment, zone0Payment, zone1Payment, zone2Payment, overWeight, routeIncentiveParcel, green, yellow, timeIncentive, weightIncentive, routeIncentive, "2023-05-01"))
            .collect(Collectors.toList());

        sqls.forEach(s -> System.out.println(s));

    }

    @Test
    public void testGenDrivers() {
        final List<Map<String, Integer>> drivers = genDrivers();
        final List<String> sqls = drivers.stream().map(d -> String.format(driverSql,
                d.get("clientId"),
                d.get("deliveryCompanyId"),
                d.get("fleetId"),
                d.get("driverId"),
                d.get("totalParcel"),
                d.get("totalParcel"),
                d.get("zone0Parcel"),
                d.get("zone1Parcel"),
                d.get("zone2Parcel"),
                d.get("totalPayment"),
                d.get("tax"),
                d.get("totalPaid"),
                d.get("zone0"),
                d.get("zone1"),
                d.get("zone2"),
                d.get("overWeight"),
                d.get("routeIncentiveParcel"),
                d.get("greenZoneParcel"),
                d.get("yellowZoneParcel"),
                d.get("timeIncentive"),
                d.get("weightIncentive"),
                d.get("routeIncentive")))
            .collect(Collectors.toList());

        sqls.forEach(s -> System.out.println(s));
    }

    @Ignore
    @Test
    public void testGenAndExecuteSqls() {
        final List<String> dates = genDates(LocalDate.of(2023, 4, 1), LocalDate.of(2023, 5, 21));
        final List<String> sqls = dates.stream().flatMap(d -> genSqls(d).stream()).collect(Collectors.toList());
        sqls.forEach(sql -> DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new), sql)
            .execute());
    }

}
