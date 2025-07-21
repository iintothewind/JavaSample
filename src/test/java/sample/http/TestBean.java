package sample.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
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
