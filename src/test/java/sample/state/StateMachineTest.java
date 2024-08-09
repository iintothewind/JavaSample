package sample.state;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import sample.sm.Order;
import sample.sm.StateMachine;
import sample.sm.StateMachine.Builder;
import sample.sm.Status;

public class StateMachineTest {


    @Test
    public void testStateMachine01() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(null, Status.TO_DO, o -> Objects.isNull(o.getStatus()), o -> o.withStatus(Status.TO_DO))
            .build();

        final boolean result = sm.testTransition(null, Status.CREATED, null);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void testStateMachine02() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(null, Status.TO_DO, o -> Objects.isNull(o.getStatus()), o -> o.withStatus(Status.TO_DO))
            .build();

        final boolean result = sm.testTransition(null, Status.CREATED, null);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void testStateMachine03() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.equals(Status.CREATED, o.getStatus()), o -> o.withStatus(Status.TO_DO))
            .build();

        final boolean result = sm.testTransition(Status.CREATED, Status.CREATED, null);
        Assertions.assertThat(result).isFalse();
    }


    @Test
    public void testStateMachine05() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.equals(Status.CREATED, o.getStatus()), o -> o.withStatus(Status.TO_DO))
            .build();

        final boolean result = sm.testTransition(Status.CREATED, Status.TO_DO, Order.builder().status(Status.CREATED).build());
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testStateMachine06() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.equals(Status.CREATED, o.getStatus()), o -> o.withStatus(Status.TO_DO))
            .withTransitions(Set.of(Status.DONE, Status.TEST, Status.IN_PROGRESS), Status.TO_DO,
                o -> List.of(Status.DONE, Status.TEST, Status.IN_PROGRESS).contains(o.getStatus()),
                o -> o.withStatus(Status.TO_DO))
            .build();

        final boolean result1 = sm.testTransition(Status.CREATED, Status.TO_DO, Order.builder().status(Status.CREATED).build());
        Assertions.assertThat(result1).isTrue();
        final boolean result2 = sm.testTransition(Status.TEST, Status.TO_DO, Order.builder().id("a").build());
        Assertions.assertThat(result2).isTrue();
        final boolean result3 = sm.testTransition(Status.IN_PROGRESS, Status.TO_DO, Order.builder().id("a").build());
        Assertions.assertThat(result3).isTrue();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine07() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.equals(Status.CREATED, o.getStatus()), Function.identity())
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.equals(Status.CREATED, o.getStatus()), Function.identity())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine08() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransitions(Set.of(Status.CREATED, Status.TEST, Status.IN_PROGRESS), Status.TO_DO,
                o -> List.of(Status.DONE, Status.TEST, Status.IN_PROGRESS).contains(o.getStatus()),
                Function.identity())
            .withTransitions(Set.of(Status.CREATED, Status.TEST, Status.IN_PROGRESS), Status.TO_DO,
                o -> List.of(Status.DONE, Status.TEST, Status.IN_PROGRESS).contains(o.getStatus()),
                Function.identity())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine09() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.equals(Status.CREATED, o.getStatus()), Function.identity())
            .withTransitions(Status.CREATED, Set.of(Status.TO_DO), o -> Objects.equals(Status.CREATED, o.getStatus()), Function.identity())
            .build();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine10() {
        final StateMachine<Status, Order, Order> sm = Builder
            .typeOf(Status.class, Order.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.equals(Status.CREATED, o.getStatus()), Function.identity())
            .withTransitions(Status.CREATED, Set.of(Status.DONE, Status.TO_DO), o -> Objects.equals(Status.CREATED, o.getStatus()), Function.identity())
            .build();
    }
}
