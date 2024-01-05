package sample.jooq;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.junit.Test;
import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import io.vavr.control.Try;
import sample.http.JsonUtil;


public class SettlementDetailTestDataGenerator {
    private HikariDataSource dataSource = null;
    private String sql = "INSERT INTO settlement_details" +
        "(client, deliveryCompany, fleet, driver, route, otp, totalParcel, zone1Parcel, zone2Parcel, zone3Parcel, totalAmount, tax, totalPaid, zone1, zone2, zone3, overWeight, routeIncentiveParcel, greenZoneParcel, yellowZoneParcel, timeIncentive, weightIncentive, routeIncentive, settlementDate, createdDate, updatedDate) " +
        "values " +
        "(?,      ?,               ?,      ?,     ?,     ?,   ?,           ?,           ?,           ?,           ?,           ?,   ?,         ?,     ?,     ?,     ?,          ?,                    ?,               ?,                ?,             ?,               ?,              ?,              ?,           ?)";

    @Before
    public void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/echobase?useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("admin");
    }

    private List<Tuple2<Integer, String>> loadClients() {
        final List<Tuple2<Integer, String>> clients = DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),
                "SELECT id,clientName FROM clients")
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

    private List<String> genDates(final LocalDateTime from, final LocalDateTime to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkArgument(to.isAfter(from));
        final long n = ChronoUnit.DAYS.between(from, to);
        final List<String> dates = LongStream.range(0L, n + 1)
            .mapToObj(from::plusDays)
            .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .collect(Collectors.toList());
        return dates;
    }

    private List<Map<String, String>> genDrivers() {
        final Double zone1Rate = 3D;
        final Double zone2Rate = 4D;
        final Double zone3Rate = 5D;

        final Integer zone1Parcel = rand(30, 50);
        final Double zone1Payment = zone1Parcel * zone1Rate;
        final Integer zone2Parcel = rand(10, 20);
        final Double zone2Payment = zone2Parcel * zone2Rate;
        final Integer zone3Parcel = rand(1, 10);
        final Double zone3Payment = zone3Parcel * zone3Rate;
        final Integer totalParcel = zone1Parcel + zone2Parcel + zone3Parcel;
        final Double overWeight = Double.valueOf(rand(1, 50).toString());
        final Integer routeIncentiveParcel = rand(1, 30);
        final Integer green = rand(10, 40);
        final Integer yellow = rand(1, 20);
        final Double timeIncentive = green * 0.15d;
        final Double weightIncentive = overWeight * 0.15d;
        final Double routeIncentive = routeIncentiveParcel * 0.15d;
        final Double totalPayment = zone1Payment + zone2Payment + zone3Payment + timeIncentive + weightIncentive + routeIncentive;
        final Double tax = totalPayment * 0.15d;
        final Double totalPaid = (totalPayment - tax) * 0.75d;
        final Double otp = rand(40, 80) / 100d;

        final List<Map<String, String>> drivers = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .flatMap(f -> loadDrivers(f._3).stream().map(d -> Tuple.of(f._1, f._2, f._3, d._1)))
            .map(d -> HashMap.<String, String>empty()
                .put("clientId", d._1.toString())
                .put("deliveryCompanyId", d._2.toString())
                .put("fleetId", d._3.toString())
                .put("driverId", d._4.toString())
                .put("route", "0")
                .put("otp", otp.toString())
                .put("totalParcel", totalParcel.toString())
                .put("zone1Parcel", zone1Parcel.toString())
                .put("zone2Parcel", zone2Parcel.toString())
                .put("zone3Parcel", zone3Parcel.toString())
                .put("totalPayment", totalPayment.toString())
                .put("tax", tax.toString())
                .put("totalPaid", totalPaid.toString())
                .put("zone1", zone1Payment.toString())
                .put("zone2", zone2Payment.toString())
                .put("zone3", zone3Payment.toString())
                .put("overWeight", overWeight.toString())
                .put("routeIncentiveParcel", routeIncentiveParcel.toString())
                .put("greenZoneParcel", green.toString())
                .put("yellowZoneParcel", yellow.toString())
                .put("timeIncentive", timeIncentive.toString())
                .put("weightIncentive", weightIncentive.toString())
                .put("routeIncentive", routeIncentive.toString())
                .toJavaMap())
            .collect(Collectors.toList());

        return drivers;
    }

    private List<Map<String, String>> genFleets(final List<Map<String, String>> drivers) {
        final List<Map<String, String>> fleets = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .flatMap(dc -> loadFleets(dc._2).stream().map(f -> Tuple.of(dc._1, dc._2, f._1)))
            .map(f -> Tuple.of(f, drivers.stream().filter(m -> Objects.equals(m.getOrDefault("fleetId", "0"), f._3.toString())).collect(Collectors.toList())))
            .map(t -> HashMap.<String, String>empty()
                .put("clientId", t._1._1.toString())
                .put("deliveryCompanyId", t._1._2.toString())
                .put("fleetId", t._1._3.toString())
                .put("route", "0")
                .put("otp", t._2.size() == 0 ? "0" : Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("otp", "0"))).sum() / t._2.size()))
                .put("totalParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("totalParcel", "0"))).sum()))
                .put("zone1Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone1Parcel", "0"))).sum()))
                .put("zone2Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone2Parcel", "0"))).sum()))
                .put("zone3Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone3Parcel", "0"))).sum()))
                .put("totalPayment", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("totalPayment", "0"))).sum()))
                .put("tax", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("tax", "0"))).sum()))
                .put("totalPaid", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("totalPaid", "0"))).sum()))
                .put("zone1", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone1", "0"))).sum()))
                .put("zone2", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone2", "0"))).sum()))
                .put("zone3", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone3", "0"))).sum()))
                .put("overWeight", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("overWeight", "0"))).sum()))
                .put("routeIncentiveParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("routeIncentiveParcel", "0"))).sum()))
                .put("greenZoneParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("greenZoneParcel", "0"))).sum()))
                .put("yellowZoneParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("yellowZoneParcel", "0"))).sum()))
                .put("timeIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("timeIncentive", "0"))).sum()))
                .put("weightIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("weightIncentive", "0"))).sum()))
                .put("routeIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("routeIncentive", "0"))).sum()))
                .toJavaMap())
            .collect(Collectors.toList());
        return fleets;
    }

    private List<Map<String, String>> genDeliveryCompanies(final List<Map<String, String>> fleets) {
        final List<Map<String, String>> deliveryCompanies = loadClients().stream()
            .flatMap(c -> loadDeliveryCompanies(c._1).stream().map(dc -> Tuple.of(c._1, dc._1)))
            .map(dc -> Tuple.of(dc, fleets.stream().filter(f -> Objects.equals(f.getOrDefault("deliveryCompanyId", "0"), dc._2.toString())).collect(Collectors.toList())))
            .map(t -> HashMap.<String, String>empty()
                .put("clientId", t._1._1.toString())
                .put("deliveryCompanyId", t._1._2.toString())
                .put("route", "0")
                .put("otp", t._2.size() == 0 ? "0" : Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("otp", "0"))).sum() / t._2.size()))
                .put("totalParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("totalParcel", "0"))).sum()))
                .put("zone1Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone1Parcel", "0"))).sum()))
                .put("zone2Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone2Parcel", "0"))).sum()))
                .put("zone3Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone3Parcel", "0"))).sum()))
                .put("totalPayment", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("totalPayment", "0"))).sum()))
                .put("tax", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("tax", "0"))).sum()))
                .put("totalPaid", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("totalPaid", "0"))).sum()))
                .put("zone1", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone1", "0"))).sum()))
                .put("zone2", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone2", "0"))).sum()))
                .put("zone3", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone3", "0"))).sum()))
                .put("overWeight", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("overWeight", "0"))).sum()))
                .put("routeIncentiveParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("routeIncentiveParcel", "0"))).sum()))
                .put("greenZoneParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("greenZoneParcel", "0"))).sum()))
                .put("yellowZoneParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("yellowZoneParcel", "0"))).sum()))
                .put("timeIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("timeIncentive", "0"))).sum()))
                .put("weightIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("weightIncentive", "0"))).sum()))
                .put("routeIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("routeIncentive", "0"))).sum()))
                .toJavaMap())
            .collect(Collectors.toList());
        return deliveryCompanies;
    }

    private List<Map<String, String>> genClients(final List<Map<String, String>> deliveryCompanies) {
        final List<Map<String, String>> clients = loadClients().stream()
            .map(c -> Tuple.of(c, deliveryCompanies.stream().filter(dc -> Objects.equals(dc.getOrDefault("clientId", "0"), c._1.toString())).collect(Collectors.toList())))
            .map(t -> HashMap.<String, String>empty()
                .put("clientId", t._1._1.toString())
                .put("route", "0")
                .put("otp", t._2.size() == 0 ? "0" : Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("otp", "0"))).sum() / t._2.size()))
                .put("totalParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("totalParcel", "0"))).sum()))
                .put("zone1Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone1Parcel", "0"))).sum()))
                .put("zone2Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone2Parcel", "0"))).sum()))
                .put("zone3Parcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("zone3Parcel", "0"))).sum()))
                .put("totalPayment", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("totalPayment", "0"))).sum()))
                .put("tax", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("tax", "0"))).sum()))
                .put("totalPaid", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("totalPaid", "0"))).sum()))
                .put("zone1", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone1", "0"))).sum()))
                .put("zone2", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone2", "0"))).sum()))
                .put("zone3", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("zone3", "0"))).sum()))
                .put("overWeight", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("overWeight", "0"))).sum()))
                .put("routeIncentiveParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("routeIncentiveParcel", "0"))).sum()))
                .put("greenZoneParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("greenZoneParcel", "0"))).sum()))
                .put("yellowZoneParcel", Integer.toString(t._2.stream().mapToInt(m -> Integer.parseInt(m.getOrDefault("yellowZoneParcel", "0"))).sum()))
                .put("timeIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("timeIncentive", "0"))).sum()))
                .put("weightIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("weightIncentive", "0"))).sum()))
                .put("routeIncentive", Double.toString(t._2.stream().mapToDouble(m -> Double.parseDouble(m.getOrDefault("routeIncentive", "0"))).sum()))
                .toJavaMap())
            .collect(Collectors.toList());
        return clients;
    }

    private List<Map<String, String>> genEntities(final String settlementDate) {
        final List<Map<String, String>> drivers = genDrivers();
        final List<Map<String, String>> fleets = genFleets(drivers);
        final List<Map<String, String>> deliveryCompanies = genDeliveryCompanies(fleets);
        final List<Map<String, String>> clients = genClients(deliveryCompanies);
        return Stream.of(drivers, fleets, deliveryCompanies, clients)
            .flatMap(Collection::stream)
            .map(m -> HashMap.ofAll(m).put("settlementDate", settlementDate).toJavaMap())
            .collect(Collectors.toList());
    }

    private Integer rand(int min, int max) {
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
        final List<String> dates = genDates(LocalDateTime.of(2023, 4, 1, 0, 0, 0), LocalDateTime.of(2023, 5, 21, 0, 0, 0));
        System.out.println(dates);
    }

    @Test
    public void testGenDriverSettlement() {
    }

    @Test
    public void testGenDrivers() {
        final List<Map<String, String>> drivers = genDrivers();
        System.out.println(drivers);
    }

    @Test
    public void testGenEntities() {
        final List<Map<String, String>> entities = genEntities("2023-04-01");
        System.out.println(entities);
    }

    @Test
    public void testStoreWebhook() {
        final Map<String, String> webhook = HashMap.<String, String>empty().put("url", "https://dev-webhooks.techdinamics.com/mapper/SubmitFile.ashx?sender=ULALA&receiver=GTAGSM&apiKey=CD3255E3C03249D2A89FA07E5A9DF3FC&version=1&doctype=STATUS")
            .toJavaMap();
        System.out.println(String.format("webhook: %s", JsonUtil.dump(webhook)));
        final String sql = "insert into client_infos (clientName,shippingMerchant,shipper,currentCount,shippingLabelPrefix,webhook) values (?,?,?,?,?,?)";
        //        DbUtil.withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new),sql)
        //            .withBindings("ULALA","gsm@ulala.ca",20,0,"GSM",JsonUtil.dump(webhook))
        //            .execute();
    }

    @Test
    public void testGenWebHookCfg() {
        final Map<String, String> kitsWebHook = HashMap.<String, String>empty()
            .put("url", "https://us-central1-wide-gecko-214719.cloudfunctions.net/function-prod-kits-webhook-handler")
            .toJavaMap();
        System.out.println(String.format("kits webhook: %s", JsonUtil.dump(kitsWebHook)));

        final Map<String, String> ziingWebHook = HashMap.<String, String>empty()
            .put("url", "https://0476.cxtsoftware.net/CxtWebService/CxtWebServiceRest.svc/rest/process/ulalaInbound/")
            .toJavaMap();
        System.out.println(String.format("zing webhook: %s", JsonUtil.dump(ziingWebHook)));

        final Map<String, String> wuyouWebHook = HashMap.<String, String>empty()
            .put("url", "https://package.51cross-border.com/updateLogistic")
            .put("header_Authorization", "Basic dWxhbGFANTFjcm9zcy1ib3JkZXIuY29tOkdtVml4Mjg3bUlpQWdNSw==")
            .toJavaMap();
        System.out.println(String.format("wuyou webhook: %s", JsonUtil.dump(wuyouWebHook)));

        System.out.println("header_1234l".substring("header_".length()));
    }

    @Test
    public void testGroupBy() {
        final String sql = "select * from settlement_details sd where sd.client is not null and sd.deliveryCompany is not null and sd.fleet is not null and sd.driver = ? and sd.settlementDate >= ? and sd.settlementDate <= ?";
        final List<Tuple5<Integer, Double, Double, Double, LocalDate>> lst = DbUtil.withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new), sql)
            .withBindings(209,
                LocalDateTime.of(2023, 7, 1, 0, 0),
                LocalDateTime.of(2023, 7, 15, 23, 59))
            .fetch(r -> Tuple.of(r.get("driver", Integer.class), r.get("totalParcel", Double.class), r.get("totalPaid", Double.class), r.get("otp", Double.class), r.get("settlementDate", LocalDate.class)));
        System.out.println(lst);
        final Map<LocalDate, List<Tuple5<Integer, Double, Double, Double, LocalDate>>> m = lst.stream().collect(Collectors.groupingBy(r -> r._5));
        System.out.println(m);
    }

    @Test
    public void testGenAndExecuteSqls() {
        final List<String> dates = genDates(LocalDateTime.of(2023, 8, 1, 0, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0, 0));
        final List<Map<String, String>> entities = dates.stream().flatMap(d -> genEntities(d).stream()).collect(Collectors.toList());
        System.out.println(entities);
        entities.forEach(m -> DbUtil
            .withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new), sql)
            .withBindings(
                Option.of(m.get("clientId")).map(Integer::parseInt).getOrNull(),
                Option.of(m.get("deliveryCompanyId")).map(Integer::parseInt).getOrNull(),
                Option.of(m.get("fleetId")).map(Integer::parseInt).getOrNull(),
                Option.of(m.get("driverId")).map(Integer::parseInt).getOrNull(),
                Integer.parseInt(m.get("route")),
                Double.parseDouble(m.get("otp")),
                Integer.parseInt(m.get("totalParcel")),
                Integer.parseInt(m.get("zone1Parcel")),
                Integer.parseInt(m.get("zone2Parcel")),
                Integer.parseInt(m.get("zone3Parcel")),
                Double.parseDouble(m.get("totalPayment")),
                Double.parseDouble(m.get("tax")),
                Double.parseDouble(m.get("totalPaid")),
                Double.parseDouble(m.get("zone1")),
                Double.parseDouble(m.get("zone2")),
                Double.parseDouble(m.get("zone3")),
                Double.parseDouble(m.get("overWeight")),
                Integer.parseInt(m.get("routeIncentiveParcel")),
                Integer.parseInt(m.get("greenZoneParcel")),
                Integer.parseInt(m.get("yellowZoneParcel")),
                Double.parseDouble(m.get("timeIncentive")),
                Double.parseDouble(m.get("weightIncentive")),
                Double.parseDouble(m.get("routeIncentive")),
                LocalDateTime.parse(m.get("settlementDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                m.get("createdDate"),
                m.get("updatedDate"),
                m.get("token"))
            .execute());
    }

    @Test
    public void testExec() {
        DbUtil.withSql(Try.of(() -> dataSource.getConnection()).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new), "update tags set tag = ? where id = 1")
            .withBindings("test")
            .execute1();
    }
}
