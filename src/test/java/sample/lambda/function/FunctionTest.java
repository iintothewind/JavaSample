package sample.lambda.function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sample.lambda.bean.Person;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FunctionTest {
  Function<String, Person> before = mappings -> {
    Map<String, String> map = Splitter.on(",").withKeyValueSeparator(":").split(mappings);
    String name = Optional.of(map.get("name")).get();
    int age = Integer.parseUnsignedInt(map.get("age"));
    String occupation = Optional.of(map.get("occupation")).get();
    return new Person(name, age, occupation);
  };

  Function<Person, String> toString = person -> {
    return MoreObjects.toStringHelper(person).add("name", person.getName()).add("age", person.getAge()).add("occupation", person.getOccupation()).toString();
  };

  Function<Person, Map<String, String>> after = person -> {
    return ImmutableMap.of("name", person.getName(), "age", person.getAge().toString(), "occupation", person.getOccupation());
  };
  private Function<String, Integer> a = s -> Option.of(Ints.tryParse(s)).getOrElse(0);
  private Function<Integer, List<Integer>> b = ImmutableList::of;
  private Function<List<String>, String> d = l -> l.stream().findFirst().orElse("");

  private Person applyFromMappingsToPerson(String mappings, Function<String, Person> before) {
    return before.apply(mappings);
  }

  private String applyToString(Person person, Function<Person, String> function) {
    return function.apply(person);
  }

  private String applyFromMappingsToString(String mappings, Function<String, String> function) {
    return function.apply(mappings);
  }

  private Map<String, String> applyFromStringToMap(String mappings, Function<String, Map<String, String>> function) {
    return function.apply(mappings);
  }

  @Test
  public void testCompose() {
    Function<String, List<Integer>> c = a.andThen(b);
    Assertions.assertThat(ImmutableList.of(ImmutableList.of("a")).stream().map(d.andThen(a)).findFirst().orElse(0)).isEqualTo(0);
    Assertions.assertThat(c.apply("1")).contains(1);
  }

  @Test
  public void testApply() {
    Person lara = new Person("Lara", 18, "student");
    Assertions.assertThat(applyToString(lara, toString)).isEqualTo("Person{name=Lara, age=18, occupation=student}");
    Assertions.assertThat(applyFromMappingsToPerson("name:Lara,age:18,occupation:student", before)).isEqualTo(lara);
  }

  @Test
  public void compose() {
    String composed = applyFromMappingsToString("name:Sarah,age:2,occupation:baby", toString.compose(before));
    Assertions.assertThat(composed).isEqualTo("Person{name=Sarah, age=2, occupation=baby}");
  }

  @Test
  public void after() {
    Map<String, String> mappings = applyFromStringToMap("name:Sarah,age:2,occupation:baby", before.andThen(after));
    Assertions.assertThat(mappings).isEqualTo(ImmutableMap.of("name", "Sarah", "age", "2", "occupation", "baby"));
  }

  @Test
  public void identity() {

  }
}
