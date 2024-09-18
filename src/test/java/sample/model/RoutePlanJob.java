package sample.model;

import io.vavr.API;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@With
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoutePlanJob {

    public final static Integer inProgressJobLimit = 5;
    public final static Integer attemptLimit = 5;

    public final static String JOB_TYPE_ROUTE = "route";
    public final static String JOB_TYPE_CLUSTER = "cluster";

    public final static String PLAN_JOB_STATUS_SUBMITTED = "submitted";
    public final static String PLAN_JOB_STATUS_IN_PROGRESS = "in_progress";
    public final static String PLAN_JOB_STATUS_OPTIMIZED = "optimized";
    public final static String PLAN_JOB_STATUS_PARTIALLY_OPTIMIZED = "partially_optimized";
    public final static String PLAN_JOB_STATUS_FAILED = "failed";

    public final static String baseUrl = "https://graphhopper.com/api/1";

    public final static String key = "0fb3c0af-ff6b-442f-86af-2d48887d1533";

    @EqualsAndHashCode.Include
    private Integer id;

    @Builder.Default
    private String jobType = JOB_TYPE_ROUTE;

    @EqualsAndHashCode.Include
    private Integer planId;

    @EqualsAndHashCode.Include
    private String jobId;

    private String request;

    private String response;

    private Integer actionUser;

    private String status;

    @Builder.Default
    private Integer attempts = 0;

    private LocalDateTime requestTime;

    private LocalDateTime respondTime;

    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    public String parseStatus(final GhOptimizeResp resp) {
        if (Objects.nonNull(attempts) && attempts > RoutePlanJob.attemptLimit) {
            return RoutePlanJob.PLAN_JOB_STATUS_FAILED;
        }
        if (Objects.nonNull(resp)) {
            final String status = API.Match(resp).of(
                API.Case(API.$(r -> resp.isSuccessful()), PLAN_JOB_STATUS_OPTIMIZED),
                API.Case(API.$(r -> resp.isPartiallySuccessful()), PLAN_JOB_STATUS_PARTIALLY_OPTIMIZED),
                API.Case(API.$(r -> resp.isInProgress()), PLAN_JOB_STATUS_IN_PROGRESS),
                API.Case(API.$(), this.status));
            return status;
        }
        return status;
    }
}