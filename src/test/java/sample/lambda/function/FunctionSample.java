package sample.lambda.function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import org.assertj.core.api.Assertions;
import sample.lambda.bean.Person;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FunctionSample {
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

  private Function<String, Integer> a = Ints::tryParse;

  private Function<Integer, List<Integer>> b = ImmutableList::of;

  private Function<List<String>, String> d = l -> l.stream().findFirst().orElse("");

  @Test
  public void testCompose() {
    Function<String, List<Integer>> c = a.andThen(b);
    ImmutableList.of(ImmutableList.of("a")).stream().map(d.andThen(a)).findFirst().get();
    Assertions.assertThat(c.apply("1")).contains(1);
  }

  @Test
  public void testApply() {
    Person lara = new Person("Lara", 18, "student");
    Assert.assertEquals("Person{name=Lara, age=18, occupation=student}", applyToString(lara, toString));
    Assert.assertEquals(lara, applyFromMappingsToPerson("name:Lara,age:18,occupation:student", before));
  }

  @Test
  public void compose() {
    String composed = applyFromMappingsToString("name:Sarah,age:2,occupation:baby", toString.compose(before));
    Assert.assertEquals("Person{name=Sarah, age=2, occupation=baby}", composed);
  }

  @Test
  public void after() {
    Map<String, String> mappings = applyFromStringToMap("name:Sarah,age:2,occupation:baby", before.andThen(after));
    Assert.assertEquals(ImmutableMap.of("name", "Sarah", "age", "2", "occupation", "baby"), mappings);
  }

  @Test
  public void identity() {

  }
}
