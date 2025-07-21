package sample.lambda.function;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sample.lambda.bean.Person;

import java.util.function.Consumer;

@Slf4j
public class ConsumerTest {
  private Consumer<Person> grow = person -> person.setAge(person.getAge() + 1);
  private Consumer<Person> nameChange = person -> person.setName("");

  private void growAndThenChangeName(Person person, String name) {
    Consumer<Person> nameChange = p -> p.setName(name);
    grow.andThen(nameChange).accept(person);
  }

  @Test
  public void testAccept() {
    Person sara = new Person("sarah", 2, "baby");
    grow.accept(sara);
    Assertions.assertThat(sara).isEqualTo(new Person("sarah", 3, "baby"));
  }

  @Test
  public void testAndThen() {
    Person sara = new Person("sarah", 2, "baby");
    growAndThenChangeName(sara, "eva");
    Assertions.assertThat(sara).isEqualTo(new Person("sarah", 3, "baby"));
  }
}
