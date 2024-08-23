package sample.sm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
public class Order {

    Integer id;
    String name;
    Status status;

    private final static StateMachine<Status, Order, Order> sm = StateMachine
        .Builder
        .typeOf(Status.class, Order.class, Order.class)
        .withTransition(null, Status.CREATED,
            o -> Objects.nonNull(o) && Objects.isNull(o.getStatus()),
            o -> o.withStatus(Status.CREATED))
        .withTransition(Status.CREATED, Status.TO_DO,
            o -> Objects.nonNull(o) && Status.CREATED.equals(o.getStatus()),
            o -> o.withStatus(Status.TO_DO))
        .withTransition(Status.TO_DO, Status.IN_PROGRESS,
            o -> Objects.nonNull(o) && Status.TO_DO.equals(o.getStatus()),
            o -> o.withStatus(Status.IN_PROGRESS))
        .withTransition(Status.IN_PROGRESS, Status.TEST,
            o -> Objects.nonNull(o) && Status.IN_PROGRESS.equals(o.getStatus()),
            o -> o.withStatus(Status.TEST))
        .withTransition(Status.TEST, Status.IN_PROGRESS,
            o -> Objects.nonNull(o) && Status.TEST.equals(o.getStatus()),
            o -> o.withStatus(Status.IN_PROGRESS))
        .withTransitions(Set.of(Status.CREATED, Status.TO_DO, Status.IN_PROGRESS, Status.TEST), Status.CLOSED,
            o -> Objects.nonNull(o) && List.of(Status.CREATED, Status.TO_DO, Status.IN_PROGRESS, Status.TEST).contains(o.getStatus()),
            o -> o.withStatus(Status.CLOSED))
        .build();

    public Order updateStatus(Status toStatus) {
        final Order updatedOrder = Optional.ofNullable(sm.mkTransition(getStatus(), toStatus, this)).orElse(this);
        return updatedOrder;
    }
}
