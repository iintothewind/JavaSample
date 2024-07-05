package sample.basic;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AutoTest {

    @Test
    public void testAutoValue01() {
        final Animal cat = Animal.builder().name("cat").age(2).build();
        Assertions.assertThat(cat.name()).isEqualTo("cat");
        Assertions.assertThat(cat.age()).isEqualTo(2);
    }

}
