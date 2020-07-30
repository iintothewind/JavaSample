package war;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Entry {
    private int priority;
    private int index;
    private int data;
}
