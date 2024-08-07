package sample.sm;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Order {
    private String id;

    public void act(final Status from, final Status to, final Event event) {
        System.out.printf("User: %s performed action on event: %s to change status from :%s to: %s%n", id, event, from, to);
    }
}
