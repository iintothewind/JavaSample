package sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.control.Try;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import okhttp3.Response;

@With
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GhOptimizeResp {

    public final static String STATUS_FINISHED = "finished";
    public final static String STATUS_WAITING_IN_QUEUE = "waiting_in_queue";
    public final static String STATUS_PROCESSING = "processing";

    @EqualsAndHashCode.Include
    @JsonProperty("job_id")
    private String jobId;

    private RateLimit rateLimit;

    /**
     * Indicates the current status of the job, possible values: waiting_in_queue, processing, finished
     */
    @EqualsAndHashCode.Include
    private String status;

    @JsonProperty("waiting_time_in_queue")
    private Integer waitingTimeInQueue;

    @JsonProperty("processing_time")
    private Integer processingTime;

    private Solution solution;

    private List<Cluster> clusters;

    /**
     * status code
     */
    @EqualsAndHashCode.Include
    private Integer code;

    /**
     * error message for failed request
     */
    @EqualsAndHashCode.Include
    private String message;

    private List<Hint> hints;


    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Address {

        @JsonProperty("location_id")
        private String locationId;

        private String name;

        private Double lat;

        private Double lon;

    }

    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Activity {

        /**
         * possible values: start, end, service
         */
        private String type;

        @JsonProperty("location_id")
        private String locationId;

        private Address address;

        private Integer distance;
    }

    /**
     * error hints for failed request
     */
    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Hint {

        private String message;
        private String details;

    }

    /**
     * route planning point
     */
    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Point {

        private String type;
        private List<List<Double>> coordinates;
    }

    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Route {

        /**
         * route name
         */
        @JsonProperty("vehicle_id")
        private String vehicleId;

        private Integer distance;

        private List<Activity> activities;

        private List<Point> points;
    }

    /**
     * reason for unassigned services or shipments
     */
    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnassignDetail {

        /**
         * Id of unassigned service/shipment
         */
        private String id;

        // @formatter:off
        /**
         * Code	Reason
         * 1	cannot serve required skill
         * 2	cannot be visited within time window
         * 3	does not fit into any vehicle due to capacity
         * 4	cannot be assigned due to max distance constraint of vehicles
         * 21	could not be assigned due to relation constraint
         * 22	could not be assigned due to allowed vehicle constraint
         * 23	could not be assigned due to max-time-in-vehicle constraint
         * 24	driver does not need a break
         * 25	could not be assigned due to disallowed vehicle constraint
         * 26	could not be assigned due to max drive time constraint
         * 27	could not be assigned due to max job constraint
         * 28	could not be assigned due to max activity constraint
         * 29	could not be assigned due to group relation constraint
         * 50	underlying location cannot be accessed over road network by at least one vehicle
         */
        // @formatter:on
        private Integer code;

    }

    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Unassigned {

        private List<GhOptimizeReq.Service> services;

        private List<UnassignDetail> details;

    }

    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Solution {

        private Integer distance;

        /**
         * Number of employed vehicles.
         */
        @JsonProperty("no_vehicles")
        private Integer numOfVehicles;
        /**
         * Number of jobs that could not be assigned to final solution.
         */
        @JsonProperty("no_unassigned")
        private Integer numOfUnassigned;

        private List<Route> routes;

        private Unassigned unassigned;

    }

    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Cluster {

        private String name;
        private Address center;
        private Integer quantity;
        private List<String> ids;
    }

    @With
    @Getter
    @Setter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RateLimit {

        public final static Long dailyLimit = 100_000L;
        public final static Long minutelyLimit = 1000L;

        public final static String creditsHeader = "X-RateLimit-Credits";
        public final static String limitHeader = "X-RateLimit-Limit";
        public final static String remainingHeader = " X-RateLimit-Remaining";
        public final static String resetHeader = " X-RateLimit-Reset";

        // @formatter:off
        /**
         * header value of X-RateLimit-Credits
         * The credit costs for this request.
         * Note it could be a decimal and even negative number,
         * e.g. when an async request failed.
         */
        // @formatter:on
        private Long credits;

        /**
         * header value of X-RateLimit-Limit, current daily credit limit
         */
        private Long limit;

        /**
         * header value of X-RateLimit-Remaining, remaining daily credits until the reset
         */
        private Long remaining;

        /**
         * header value of X-RateLimit-Reset, The number of seconds that you have to wait before a reset of the credit count is done
         */
        private Long reset;
    }

    public boolean isRouteOptResp() {
        return Objects.equals(STATUS_FINISHED, status) && Objects.nonNull(solution) && Objects.isNull(clusters);
    }

    public boolean isClusterResp() {
        return Objects.equals(STATUS_FINISHED, status) && Objects.isNull(solution) && Objects.nonNull(clusters);
    }

    public boolean isSuccessful() {
        if (isRouteOptResp()) {
            final Integer numOfUnassigned = Optional.ofNullable(solution).map(Solution::getNumOfUnassigned).orElse(0);
            return numOfUnassigned == 0;
        } else {
            return isClusterResp();
        }
    }

    public boolean isPartiallySuccessful() {
        if (isRouteOptResp()) {
            final Integer numOfUnassigned = Optional.ofNullable(solution).map(Solution::getNumOfUnassigned).orElse(0);
            return numOfUnassigned > 0;
        }
        return false;
    }

    public boolean isInProgress() {
        return (Objects.nonNull(jobId) && Objects.isNull(status) && Objects.isNull(solution) && Objects.isNull(clusters))
            || STATUS_PROCESSING.equalsIgnoreCase(status)
            || STATUS_WAITING_IN_QUEUE.equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return !isSuccessful() && !isPartiallySuccessful() && !isInProgress();
    }

    public static RateLimit buildRateLimit(final Response r) {
        final RateLimit rateLimit = RateLimit.builder()
            .credits(Try.of(() -> Long.parseLong(Objects.requireNonNull(r.header(RateLimit.creditsHeader)))).getOrNull())
            .limit(Try.of(() -> Long.parseLong(Objects.requireNonNull(r.header(RateLimit.limitHeader)))).getOrNull())
            .remaining(Try.of(() -> Long.parseLong(Objects.requireNonNull(r.header(RateLimit.remainingHeader)))).getOrNull())
            .reset(Try.of(() -> Long.parseLong(Objects.requireNonNull(r.header(RateLimit.resetHeader)))).getOrNull())
            .build();

        return rateLimit;
    }
}
