package sample.http;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class JsonUtilTest {

    @Test
    public void testDiffObj01() {
        final TestBean t1 = TestBean.builder().a("ssss").b("dddd").build();
        final TestBean t2 = TestBean.builder().a("aaab").b("dddd").build();
        final TestBean t3 = JsonUtil.diffObj(t1, t2);
        Assertions.assertThat(t3).matches(t -> Objects.equals(t.a, "aaab") && Objects.isNull(t.b));
    }

}
