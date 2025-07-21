package sample.lambda.function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sample.lambda.bean.Person;

import java.util.function.Supplier;

public class SupplierTest {

  private Person getPersonBySupplier(String name, int age, String occupation) {
    Supplier<Person> personSupplier = () -> new Person(name, age, occupation);
    return personSupplier.get();
  }

  @Test
  public void testGet() {
    Assertions.assertThat(getPersonBySupplier("sarah", 2, "baby")).isEqualTo(new Person("sarah", 2, "baby"));
  }
}
