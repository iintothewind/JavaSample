package sample.http;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class TestBean {
    String a;
    String b;

    public String getB() {
        if (b != null) {
            return b.toUpperCase();
        }
        return b;
    }
}
