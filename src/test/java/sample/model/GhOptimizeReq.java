package sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import sample.http.JsonUtil;


@With
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GhOptimizeReq {

    /**
     * Specifies the available vehicles. In our
     */
    private List<Vehicle> vehicles;

    @JsonProperty("vehicle_types")
    private List<VehicleType> vehicleTypes;

    // @formatter:off
    /**
     * Specifies the orders of the type "service".
     * These are, for example, pick-ups, deliveries or other stops that are to be approached by the specified vehicles.
     * Each of these orders contains only one location.
     */
    // @formatter:on
    private List<Service> services;

    /**
     * Defines additional relationships between orders.
     */
    private List<JobRelation> relations;

    /**
     * Specifies an objective function. The vehicle routing problem is solved in such a way that this objective function is minimized.
     */
    private List<Objective> objectives;

    /**
     * Specifies general configurations.
     */
    private Configuration configuration;

    /**
     * detail of cluster config
     */
    private List<Cluster> clusters;


    /**
     * all stops of clusters
     */
    private List<Customer> customers;


    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Vehicle {

        /**
         * required, It should be route.name in ulala system. Specifies the ID of the vehicle. Ids must be unique, i.e. if there are two vehicles with the same ID, an error is returned.
         */
        @NonNull
        @JsonProperty("vehicle_id")
        private String vehicleId;

        @JsonProperty("type_id")
        @Builder.Default
        private String typeId = VehicleType.defaultTypeId;

        @JsonProperty("start_address")
        private Address startAddress;

        @JsonProperty("end_address")
        private Address endAddress;
        /**
         * Specifies the maximum number of jobs a vehicle can load.
         */
        @JsonProperty("max_jobs")
        @Builder.Default
        private Integer maxJobs = 600;

        /**
         * Specifies the maximum number of activities a vehicle can conduct.
         */
        @JsonProperty("max_activities")
        private Integer maxActivities;

        private List<String> skills;

        @Builder.Default
        @JsonProperty("return_to_depot")
        private Boolean returnToDepot = Boolean.TRUE;
    }


    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VehicleType {

        public final static String defaultTypeId = "ulala_car";
        public static String routeProfile = "car_avoid_toll";

        /**
         * required. Specifies the id of the vehicle type. If a vehicle needs to be of this type, it should refer to this with its type_id attribute.
         */
        @JsonProperty("type_id")
        @Builder.Default
        private String typeId = defaultTypeId;

        // @formatter:off
        /**
         *
         * Default: [0] Specifies an array of capacity dimension values which need to be int values.
         * For example, if there are two dimensions such as volume and weight then it needs to be defined as [ 1000, 300 ]
         * assuming a maximum volume of 1000 and a maximum weight of 300.
         */
        // @formatter:on
        @Builder.Default
        private List<Integer> capacity = ImmutableList.of(600);

        /**
         * <a href="https://docs.graphhopper.com/#section/Map-Data-and-Routing-Profiles/OpenStreetMap">profile values</a>
         */
        @Builder.Default
        private String profile = routeProfile;

        @Builder.Default
        @JsonProperty("consider_traffic")
        private Boolean considerTraffic = Boolean.FALSE;
    }

    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Address {

        /**
         * required, it should be order.id in ulala system
         */
        @JsonProperty("location_id")
        private String orderId;

        /**
         * required, It should be order.trackNumber in ulala system.
         */
        @JsonProperty("name")
        private String trackNumber;

        /**
         * required, Longitude of location.
         */
        @NonNull
        private Double lon;

        /**
         * required, Latitude of location.
         */
        @NonNull
        private Double lat;

        // @formatter:off
        /**
         * Optional parameter. 
         * Provide a hint that includes only the street name for each address to better snap the coordinates (lon,lat) to road network. 
         * E.g. if there is an address or house with two or more neighboring streets you can control for which street the closest location is looked up.
         */
        // @formatter:on
        @JsonProperty("street_hint")
        private String streetHint;

        // @formatter:off
        /**
         * Default: "any"
         * Optional parameter. Specifies on which side a point should be relative to the driver when she leaves/arrives at a start/target/via point.
         * Only supported for motor vehicles and OpenStreetMap.
         * Enum: "right" "left" "any"
         */
        // @formatter:on
        private String curbside;

    }

    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Service {

        // @formatter:off
        /**
         * required.
         * It should be order.trackNumber in ulala system.
         * Specifies the id of the service.
         * Ids need to be unique so there must not be two services/shipments with the same id.
         */
        // @formatter:on
        @NonNull
        private String id;

        @Builder.Default
        private Integer priority = 2;

        // @formatter:off
        /**
         * Meaningful name for service
         * It should either be order.trackNumber or order.recipientName
         */
        // @formatter:on
        private String name;

        private Address address;

        // @formatter:off
        /**
         * Default: [0]
         * Size can have multiple dimensions and should be in line with the capacity dimension array of the vehicle type.
         * For example, if the item that needs to be delivered has two size dimension, volume and weight,
         * then specify it as following [ 20, 5 ] assuming a volume of 20 and a weight of 5.
         */
        // @formatter:on
        private List<Integer> size;

        private String group;

    }

    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobRelation {

        public final static String inSequence = "in_sequence";
        public final static String inDirectSequence = "in_direct_sequence";
        public final static String inSameRoute = "in_same_route";
        public final static String neighbor = "neighbor";

        // @formatter:off
        /**
         * Specifies the type of relation. It must be either of type in_same_route, not_in_same_route, in_sequence, in_direct_sequence or neighbor.
         * in_same_route: As the name suggest, it enforces the specified services or shipments to be in the same route.
         */
        // @formatter:on
        @Builder.Default
        private String type = inSequence;

        // @formatter:off
        /**
         * Specifies an array of shipment and/or service ids that are in relation.
         * If you deal with services then you need to use the id of your services in ids.
         * To also consider sequences of the pickups and deliveries of your shipments, you need to use a special ID,
         * i.e. use your shipment id plus the keyword _pickup or _delivery.
         * If you want to place a service or shipment activity at the beginning of your route, use the special ID start.
         * In turn, use end to place it at the end of the route.
         */
        // @formatter:on
        private List<String> ids;


        /**
         * Id of pre-assigned vehicle, i.e. the vehicle id that is determined to conduct the services and shipments in this relation.
         */
        @JsonProperty("vehicle_id")
        private String vehicleId;
    }


    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Objective {

        // @formatter:off
        /**
         * Default: "min"
         * Type of objective function, i.e. min or min-max.
         * min: Minimizes the objective value.
         * min-max: Minimizes the maximum objective value.
         *
         * For instance,
         * min -> vehicles minimizes the number of employed vehicles.
         * min -> completion_time minimizes the sum of your vehicle routes' completion time.
         *
         * If you use, for example,
         * min-max -> completion_time,
         * it minimizes the maximum of your vehicle routes' completion time, i.e. it minimizes the overall make span.
         * This only makes sense if you have more than one vehicle.
         * In case of one vehicle, switching from min to min-max should not have any impact.
         * If you have more than one vehicle, then the algorithm tries to constantly move stops from one vehicle to another such that the completion time of longest vehicle route can be further reduced.
         * For example, if you have one vehicle that takes 8 hours to serve all customers,
         * adding another vehicle (and using min-max) might halve the time to serve all customers to 4 hours.
         * However, this usually comes with higher transport costs.
         */
        // @formatter:on
        @Builder.Default
        private String type = "min";

        // @formatter:off
        /**
         * Default: "transport_time"
         * The value of the objective function.
         * The objective value transport_time solely considers the time your drivers spend on the road,
         * i.e. transport time.
         * In contrary to transport_time, completion_time also takes waiting times at customer sites into account.
         * The completion_time of a route is defined as the time from starting to ending the route,
         * i.e. the route's transport time, the sum of waiting times plus the sum of activity durations.
         * The completion_time_last_stop, on the other hand, refers to the completion time of the very last order in a tour or,
         * in other words, the completion time without the last section from the last stop to the end of the tour.
         * This is typically used if the orders are to be processed as quickly as possible.
         * The objective value vehicles can only be used along with min and minimizes vehicles.
         *
         * Enum: "completion_time" "completion_time_last_stop" "transport_time" "vehicles" "activities"
         */
        // @formatter:on
        @Builder.Default
        private String value = "completion_time";

    }

    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Routing {

        public static String clusterProfile = "as_the_crow_flies";

        // @formatter:off
        /**
         * Default: False.
         * It lets you specify whether the API should provide you with route geometries for vehicle routes or not.
         * Thus, you do not need to do extra routing to get the polyline for each route.
         */
        // @formatter:on
        @JsonProperty("calc_points")
        private Boolean calcPoints;

        // @formatter:off
        /**
         * The routing profile.
         * It determines the network, speed and other physical attributes used when computing the route.
         * use the profile ‘as_the_crow_flies’, then the credit costs are equal to the number of customers
         */
        // @formatter:on
        private String profile;

        /**
         * default 0
         */
        @JsonProperty("cost_per_second")
        private Integer costPerSec;

        /**
         * default 1
         */
        @JsonProperty("cost_per_meter")
        private Integer costPerMeter;

    }


    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Clustering {

        @JsonProperty("num_clusters")
        @Builder.Default
        private Integer numOfClusters = 1;

        @JsonProperty("min_quantity")
        private Integer minQuantity;

        @JsonProperty("max_quantity")
        private Integer maxQuantity;
    }


    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Cluster {

        private String name;
        @JsonProperty("min_quantity")
        private Integer minQuantity;

        @JsonProperty("max_quantity")
        private Integer maxQuantity;

        private Address center;
    }

    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Configuration {

        private Routing routing;

        private Clustering clustering;
    }

    /**
     * the point to te clustered
     */
    @With
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Customer {

        /**
         * required field, It should be order.trackNumber in ulala system.
         */
        @NonNull
        private String id;
        /**
         * required field
         */
        @NonNull
        private Address address;

        @Builder.Default
        private Integer quantity = 1;

    }

    public static Boolean isRouteOptimizeReq(final GhOptimizeReq req) {
        return Objects.nonNull(req) && Objects.nonNull(req.getVehicles()) && Objects.nonNull(req.getServices()) && Objects.isNull(req.getCustomers());
    }

    public static Boolean isClusterOptimizeReq(final GhOptimizeReq req) {
        return Objects.nonNull(req) && Objects.isNull(req.getVehicles()) && Objects.isNull(req.getServices()) && Objects.nonNull(req.getCustomers());
    }


    public static double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadiusMeters = 6378137d; // Earth's radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }

    private static Tuple2<Address, Address> mkEntryKey(@NonNull final GhOptimizeReq.Address a1, @NonNull final GhOptimizeReq.Address a2) {
        final Address minStop = ImmutableList.of(a1, a2).stream().min(Comparator.comparing(Address::getOrderId)).orElse(null);
        final Address maxStop = ImmutableList.of(a1, a2).stream().max(Comparator.comparing(Address::getOrderId)).orElse(null);
        return Tuple.of(minStop, maxStop);
    }

    public static Tuple2<Address, Address> findFarthestPoints(final List<Address> stops) {
        Preconditions.checkArgument(Objects.nonNull(stops) && stops.size() >= 2, "findFarthestPoints requires at least two stops");
        final Cache<Tuple2<Address, Address>, Double> distanceMap = CacheBuilder.newBuilder().build();
        Optional.ofNullable(stops).orElse(ImmutableList.of())
                .parallelStream()
                .filter(o -> Objects.nonNull(o) && Objects.nonNull(o.getOrderId()) && Objects.nonNull(o.getLat()) && Objects.nonNull(o.getLon()))
                .forEach(so1 -> {
                    Optional.ofNullable(stops).orElse(ImmutableList.of())
                            .parallelStream()
                            .filter(o -> Objects.nonNull(o) && Objects.nonNull(o.getOrderId()) && Objects.nonNull(o.getLat()) && Objects.nonNull(o.getLon()) && !Objects.equals(so1.getOrderId(), o.getOrderId()))
                            .forEach(so2 -> {
                                final Tuple2<Address, Address> key = mkEntryKey(so1, so2);
                                if (Objects.isNull(distanceMap.getIfPresent(key))) {
                                    final Double distance = haversineDistance(so1.getLat(), so1.getLon(), so2.getLat(), so2.getLon());
                                    distanceMap.put(key, distance);
                                }
                            });
                });
        return distanceMap.asMap().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    public static GhOptimizeReq mkRouteOptReqBody(final String routeName, final List<Order> orders) {
        Preconditions.checkArgument(Objects.nonNull(routeName), "routeName is required");
        Preconditions.checkArgument(Objects.nonNull(orders) && orders.size() >= 2, "at least two orders are required");
        Preconditions.checkArgument(orders.stream().allMatch(o -> Objects.nonNull(o.getId()) && Objects.nonNull(o.getTrackNumber()) && Objects.nonNull(o.getLat()) && Objects.nonNull(o.getLng())),
                "order id, trackNumber, lat, lng are required for all stops.");
        final List<Address> stops = orders.stream().map(o -> Address.builder()
                .orderId(o.getId().toString())
                .trackNumber(o.getTrackNumber())
                .lat(o.getLat())
                .lon(o.getLng())
                .build()).toList();
        final Tuple2<Address, Address> points = findFarthestPoints(stops);
        final GhOptimizeReq req = GhOptimizeReq
                .builder()
                .configuration(Configuration.builder().routing(Routing.builder().calcPoints(true).build()).build())
                .objectives(ImmutableList.of(Objective.builder().build()))
                .vehicleTypes(ImmutableList.of(VehicleType.builder().build()))
                .vehicles(ImmutableList.of(Vehicle.builder()
                        .vehicleId(routeName)
                        .startAddress(points._1)
                        .endAddress(points._2)
                        .build()))
                .services(stops.stream()
                        .map(a -> Service.builder()
                                .id(a.getTrackNumber())
                                .name(a.getTrackNumber())
                                .address(a).build())
                        .toList())
                .build();
        return req;
    }

    public static GhOptimizeReq mkClusterReqBody(final Map<String, Integer> clusterConfig, final List<Order> orders) {
        Preconditions.checkArgument(Objects.nonNull(clusterConfig) && clusterConfig.size() >= 2, "number of clusters must be >= 2");
        Preconditions.checkArgument(clusterConfig.values().stream().allMatch(i -> Objects.nonNull(i) && i >= 2), "number of orders for each cluster must be >=2");
        Preconditions.checkArgument(orders.stream().allMatch(o -> Objects.nonNull(o.getId()) && Objects.nonNull(o.getTrackNumber()) && Objects.nonNull(o.getLat()) && Objects.nonNull(o.getLng())),
                "order id, trackNumber, lat, lng are required for all stops.");
        Preconditions.checkArgument(clusterConfig.values().stream().mapToInt(i -> i).sum()==orders.size(), "sum of order in cluster config must be equal to size of stops.");

        final GhOptimizeReq req = GhOptimizeReq
                .builder()
                .configuration(Configuration.builder()
                        .routing(Routing.builder().profile(Routing.clusterProfile).costPerSec(0).costPerMeter(1).build())
                        .clustering(Clustering.builder()
                                .numOfClusters(clusterConfig.size())
                                .minQuantity(clusterConfig.values().stream().min(Comparator.comparingInt(i -> i)).orElse(null))
                                .maxQuantity(clusterConfig.values().stream().max(Comparator.comparingInt(i -> i)).orElse(orders.size()))
                                .build())
                        .build())
                .clusters(clusterConfig.entrySet().stream()
                        .map(kv -> Cluster.builder().name(kv.getKey()).minQuantity(kv.getValue()).maxQuantity(kv.getValue()).build())
                        .toList())
                .customers(orders.stream()
                        .map(o -> Customer.builder()
                                .id(o.getId().toString())
                                .address(Address.builder().lat(o.getLat()).lon(o.getLng()).build())
                                .build())
                        .toList())
                .build();

        return req;
    }

    /**
     * https://support.graphhopper.com/support/solutions/articles/44000718211-what-is-one-credit-
     *
     * @param req
     * @return
     */
    public static Integer estimateCreditCost(@NonNull final GhOptimizeReq req) {
        if (GhOptimizeReq.isRouteOptimizeReq(req)) {
            final Integer numOfVehicles = Optional.ofNullable(req).map(GhOptimizeReq::getVehicles).map(List::size).orElse(1);
            final Integer numOfStops = Optional.of(req).map(GhOptimizeReq::getServices).map(List::size).orElse(10);
            final Integer estimatedCost = numOfVehicles * numOfStops;
            return estimatedCost > 10 ? estimatedCost:10;
        }
        if (GhOptimizeReq.isClusterOptimizeReq(req)) {
            final Integer numOfStops = Optional.ofNullable(req).map(GhOptimizeReq::getCustomers).map(List::size).orElse(1);
            final Integer unit = Optional.ofNullable(req)
                    .map(GhOptimizeReq::getConfiguration)
                    .map(Configuration::getRouting)
                    .filter(r -> Objects.equals(Routing.clusterProfile, r.getProfile()))
                    .map(r -> 1)
                    .orElse(10);
            final Integer estimatedCost = unit * numOfStops;
            return estimatedCost > 10 ? estimatedCost:10;
        }
        throw new IllegalArgumentException(String.format("invalid request: %s", req));
    }

    public Integer estimateCreditCost() {
        return estimateCreditCost(this);
    }

    public static Request mkGetRouteSolutionReq(@NonNull final String jobId) {
        final Request request = new Request.Builder()
                .url(new HttpUrl.Builder().scheme("https").host("graphhopper.com").addPathSegment("api").addPathSegment("1").addPathSegment("vrp").addPathSegment("solution")
                        .addPathSegment(jobId).addQueryParameter("key", RoutePlanJob.key).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .get()
                .build();
        return request;
    }

    public static Request mkGetClusterSolutionReq(@NonNull final String jobId) {
        final Request request = new Request.Builder()
                .url(new HttpUrl.Builder().scheme("https").host("graphhopper.com").addPathSegment("api").addPathSegment("1").addPathSegment("cluster").addPathSegment("solution")
                        .addPathSegment(jobId).addQueryParameter("key", RoutePlanJob.key).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .get()
                .build();
        return request;
    }


    private static Request mkRouteOptimizeReq(@NonNull final GhOptimizeReq body) {
        final Request request = new Request.Builder()
                .url(new HttpUrl.Builder().scheme("https").host("graphhopper.com").addPathSegment("api").addPathSegment("1").addPathSegment("vrp").addPathSegment("optimize")
                        .addQueryParameter("key", RoutePlanJob.key).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .post(RequestBody.create(JsonUtil.dump(body), okhttp3.MediaType.parse(MediaType.APPLICATION_JSON_VALUE)))
                .build();
        return request;
    }

    private static Request mkClusterOptimizeReq(@NonNull final GhOptimizeReq body) {
        final Request request = new Request.Builder()
                .url(new HttpUrl.Builder().scheme("https").host("graphhopper.com").addPathSegment("api").addPathSegment("1").addPathSegment("cluster").addPathSegment("calculate")
                        .addQueryParameter("key", RoutePlanJob.key).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .post(RequestBody.create(JsonUtil.dump(body), okhttp3.MediaType.parse(MediaType.APPLICATION_JSON_VALUE)))
                .build();
        return request;
    }

    public static Request mkOptimizeReq(@NonNull final GhOptimizeReq body) {
        if (GhOptimizeReq.isRouteOptimizeReq(body)) {
            return mkRouteOptimizeReq(body);
        }
        if (GhOptimizeReq.isClusterOptimizeReq(body)) {
            return mkClusterOptimizeReq(body);
        }
        throw new IllegalArgumentException(String.format("invalid request: %s", body));
    }
}
