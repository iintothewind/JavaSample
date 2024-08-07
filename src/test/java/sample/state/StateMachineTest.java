package sample.state;

import java.util.Objects;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import sample.sm.Order;
import sample.sm.StateMachine;
import sample.sm.StateMachine.Builder;
import sample.sm.Status;

public class StateMachineTest {


    @Test
    public void testStateMachine01() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .defaultAllow(true)
            .build();

        final boolean result = sm.testTransition(null, Status.CREATED, null);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testStateMachine02() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .build();

        final boolean result = sm.testTransition(null, Status.CREATED, null);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void testStateMachine03() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .build();

        final boolean result = sm.testTransition(Status.CREATED, Status.CREATED, null);
        Assertions.assertThat(result).isFalse();
    }


    @Test
    public void testStateMachine05() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .build();

        final boolean result = sm.testTransition(Status.CREATED, Status.TO_DO, Order.builder().id("a").build());
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testStateMachine06() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .withTransition(Set.of(Status.CREATED, Status.TEST, Status.IN_PROGRESS), Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .build();

        final boolean result1 = sm.testTransition(Status.CREATED, Status.TO_DO, Order.builder().id("a").build());
        Assertions.assertThat(result1).isTrue();
        final boolean result2 = sm.testTransition(Status.TEST, Status.TO_DO, Order.builder().id("a").build());
        Assertions.assertThat(result2).isTrue();
        final boolean result3 = sm.testTransition(Status.IN_PROGRESS, Status.TO_DO, Order.builder().id("a").build());
        Assertions.assertThat(result3).isTrue();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine07() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine08() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .withTransition(Set.of(Status.CREATED, Status.TEST, Status.IN_PROGRESS), Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .withTransition(Set.of(Status.CREATED, Status.TEST, Status.IN_PROGRESS), Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine09() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .withTransition(Status.CREATED, Set.of(Status.TO_DO), o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .build();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testStateMachine10() {
        final StateMachine<Status, Order> sm = Builder
            .typeOf(Status.class, Order.class)
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .withTransition(Status.CREATED, Set.of(Status.DONE, Status.TO_DO), o -> Objects.nonNull(o) && Objects.nonNull(o.getId()))
            .build();
    }
}
