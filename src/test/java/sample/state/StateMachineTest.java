package sample.state;

import java.util.Objects;
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
            .withTransition(Status.CREATED, Status.TO_DO, o -> Objects.nonNull(o.getId()))
            .build();

        final boolean result = sm.testTransition(null, Status.CREATED, Order.builder().build());
        System.out.println(result);
        final boolean result1 = sm.testTransition(Status.CREATED, Status.TO_DO, Order.builder().id("a").build());
        System.out.println(result1);
    }


}
