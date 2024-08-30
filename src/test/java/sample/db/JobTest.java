package sample.db;

import com.zaxxer.hikari.HikariDataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import sample.http.JsonUtil;
import sample.model.GhOptimizeReq;
import sample.model.Order;
import sample.model.RoutePlanJob;

@Slf4j
public class JobTest {

    private static HikariDataSource dataSource = null;

    @Before
    public void setUp() {
//        dataSource = new HikariDataSource();
//        dataSource.setJdbcUrl("jdbc:mysql://localhost:13306/ulala_main?allowPublicKeyRetrieval=true&useSSL=false");
//        dataSource.setUsername("ulala");
//        dataSource.setPassword("ula123$");

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        dataSource.setJdbcUrl("jdbc:shardingsphere:classpath:sharding-local.yaml");
    }


    public static RoutePlanJob insert(@NonNull final Handle handle, final RoutePlanJob routePlanJob) {
        log.info("RoutePlanJobRepo.insert, routePlanJob: {}", routePlanJob);
        if (Objects.nonNull(routePlanJob)) {
            handle
                .createUpdate("""
                    INSERT INTO route_plan_jobs
                    (job_type, plan_id, job_id, request, response, status, attempts, action_user, request_time, respond_time, create_time)
                    VALUES(:job_type, :plan_id, :job_id, :request, :response, :status, :attempts, :action_user, :request_time, :respond_time, :create_time)
                    """)
                .bind("job_type", routePlanJob.getJobType())
                .bind("plan_id", routePlanJob.getPlanId())
                .bind("job_id", routePlanJob.getJobId())
                .bind("request", routePlanJob.getRequest())
                .bind("response", routePlanJob.getResponse())
                .bind("status", routePlanJob.getStatus())
                .bind("attempts", routePlanJob.getAttempts())
                .bind("action_user", routePlanJob.getActionUser())
                .bind("request_time", routePlanJob.getRequestTime())
                .bind("respond_time", routePlanJob.getRespondTime())
                .bind("create_time", routePlanJob.getCreateTime())
                .execute();
            final Integer lastId = handle.createQuery("SELECT LAST_INSERT_ID() as last_id").map(r -> r.getColumn("last_id", Integer.class)).findOne().orElse(null);
            return routePlanJob.withId(lastId);
        }
        return routePlanJob;
    }


    public RoutePlanJob submitRoutePlanJob(@NonNull final Integer planId, @NonNull final Integer actionUser, @NonNull final GhOptimizeReq body) {
        log.info("submitRoutePlanJob, planId: {}, actionUser: {}, body: {}", planId, actionUser, body);
        final RoutePlanJob job = Jdbi.create(dataSource).inTransaction(handle -> insert(handle, RoutePlanJob.builder()
            .jobType(GhOptimizeReq.isRouteOptimizeReq(body) ? RoutePlanJob.JOB_TYPE_ROUTE : RoutePlanJob.JOB_TYPE_CLUSTER)
            .status(RoutePlanJob.PLAN_JOB_STATUS_SUBMITTED)
            .planId(planId)
            .actionUser(actionUser)
            .request(JsonUtil.dump(body))
            .requestTime(LocalDateTime.now())
            .createTime(LocalDateTime.now())
            .build()));

        return job;
    }

    @Test
    public void testSubmitJob01() {
        final Order order1 = Order.builder().id(1820099121870626818L).trackNumber("UL8358430200").lat(49.0187454224).lng(-122.7726745605).build();
        final Order order2 = Order.builder().id(1820099121899986946L).trackNumber("UL8358430201").lat(49.0909767151).lng(-122.6120605469).build();
        final Order order3 = Order.builder().id(1820099121929347073L).trackNumber("UL8358430202").lat(49.245223999).lng(-123.0387649536).build();
        final Order order4 = Order.builder().id(1820099121954512897L).trackNumber("UL8358430203").lat(49.094379425).lng(-122.6062240601).build();
        final Order order5 = Order.builder().id(1820099121979678722L).trackNumber("UL8358430204").lat(49.2620811462).lng(-122.8924560547).build();
        final Order order6 = Order.builder().id(1820099122004844545L).trackNumber("UL8358430205").lat(49.1583786011).lng(-123.1228103638).build();
        final Order order7 = Order.builder().id(1820099122030010369L).trackNumber("UL8358430206").lat(49.3376083374).lng(-123.1145477295).build();
        final Order order8 = Order.builder().id(1820099122050981889L).trackNumber("UL8358430207").lat(49.2278175354).lng(-123.0594558716).build();
        final Order order9 = Order.builder().id(1820099122071953410L).trackNumber("UL8358430208").lat(49.2970809937).lng(-122.7877731323).build();

        final List<Order> orders = List.of(order1, order2, order3, order4, order5, order6, order7, order8, order9);
        submitRoutePlanJob(1, 1, GhOptimizeReq.mkRouteOptReqBody("route001", orders));
    }


    @Test
    public void testSubmitJob02() {
        final Order order1 = Order.builder().id(1820099121870626818L).trackNumber("UL8358430200").lat(49.0187454224).lng(-122.7726745605).build();
        final Order order2 = Order.builder().id(1820099121899986946L).trackNumber("UL8358430201").lat(49.0909767151).lng(-122.6120605469).build();
        final Order order3 = Order.builder().id(1820099121929347073L).trackNumber("UL8358430202").lat(49.245223999).lng(-123.0387649536).build();
        final Order order4 = Order.builder().id(1820099121954512897L).trackNumber("UL8358430203").lat(49.094379425).lng(-122.6062240601).build();
        final Order order5 = Order.builder().id(1820099121979678722L).trackNumber("UL8358430204").lat(49.2620811462).lng(-122.8924560547).build();
        final Order order6 = Order.builder().id(1820099122004844545L).trackNumber("UL8358430205").lat(49.1583786011).lng(-123.1228103638).build();
        final Order order7 = Order.builder().id(1820099122030010369L).trackNumber("UL8358430206").lat(49.3376083374).lng(-123.1145477295).build();
        final Order order8 = Order.builder().id(1820099122050981889L).trackNumber("UL8358430207").lat(49.2278175354).lng(-123.0594558716).build();
        final Order order9 = Order.builder().id(1820099122071953410L).trackNumber("UL8358430208").lat(49.2970809937).lng(-122.7877731323).build();

        final List<Order> orders = List.of(order1, order2, order3, order4, order5, order6, order7, order8, order9);
        submitRoutePlanJob(1, 1, GhOptimizeReq.mkClusterReqBody(Map.of("route001", 4, "route002", 5), orders));
    }

    public static List<Order> findByIds(@NonNull final Handle handle, List<Long> orderIds) {
        if (Objects.nonNull(orderIds) && !orderIds.isEmpty()) {
            final String sql = """
                SELECT id, track_number, country_code, province, city, street, address_line2, lat, lng, formatted_address
                FROM or_order oo
                WHERE oo.id in (<orderIds>)
                """;
            final List<Order> orders = handle.createQuery(sql)
                .bindList("orderIds", orderIds)
                .map(r -> Order.builder()
                    .id(r.getColumn("id", Long.class))
                    .trackNumber(r.getColumn("track_number", String.class))
                    .countryCode(r.getColumn("country_code", String.class))
                    .province(r.getColumn("province", String.class))
                    .city(r.getColumn("city", String.class))
                    .street(r.getColumn("street", String.class))
                    .addressLine2(r.getColumn("address_line2", String.class))
                    .lat(r.getColumn("lat", Double.class))
                    .lng(r.getColumn("lng", Double.class))
                    .formattedAddress(r.getColumn("formatted_address", String.class))
                    .build())
                .collectIntoList();

            return orders;
        }
        return List.of();
    }

    public List<Order> findByIds(List<Long> orderIds) {
        log.info("OrderRepo.findByIds, orderIds={}", orderIds);
        return Jdbi.create(dataSource).withHandle(handle -> findByIds(handle, orderIds));
    }

    @Test
    public void testFindOrders01() {
        final List<Order> orders = findByIds(List.of(1820099121870626818L, 1820099122004844545L, 1820099122030010369L, 1820099122050981889L));
        System.out.println(orders);
    }
}
