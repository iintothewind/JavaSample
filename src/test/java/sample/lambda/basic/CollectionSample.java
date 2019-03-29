package sample.lambda.basic;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sample.lambda.bean.Person;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionSample {
  private List<Person> people;
  private Map<String, Person> peopleMap;

  @Before
  public void setUp() {
    people = Lists.newArrayList(new Person("Ethan", 21, "Student"), new Person("Mars", 22, "Detect"), new Person("Lara", 20, "Fighter"));
    peopleMap = people.stream().collect(Collectors.toMap(Person::getName, Function.identity()));
  }

  @Test
  public void testSort() {
    people.sort(Person::compareTo);
    people.forEach(System.out::println);
  }

  @Test
  public void testForEachConsumer() {
    people.forEach(System.out::println);
  }

  @Test
  public void testRemoveAllByPredicate() {
    people.removeIf((person) -> person.getAge() > 20);
    Assert.assertEquals(1, people.size());
    Assert.assertFalse(people.contains(new Person("Ethan", 21, "Student")));
    Assert.assertFalse(people.contains(new Person("Mars", 22, "Detect")));
    Assert.assertTrue(people.contains(new Person("Lara", 20, "Fighter")));
  }

  @Test
  public void testReplace() {
    people.replaceAll((person) -> {
      if ("Lara".equalsIgnoreCase(person.getName())) {
        return new Person("Sarah", 2, "baby");
      }
      return person;
    });
    Assert.assertTrue(people.contains(new Person("Sarah", 2, "baby")));
    Assert.assertFalse(people.contains(new Person("Lara", 20, "Fighter")));
  }

  @Test
  public void testMapForEach() {
    peopleMap.forEach((k, v) -> {
      if ("Lara".equalsIgnoreCase(k)) {
        v.setAge(v.getAge() + 1);
      }
    });
    Assert.assertTrue(people.contains(new Person("Lara", 21, "Fighter")));
  }

  @Test
  public void testMapReplaceAll() {
    Person sarah = new Person("Sarah", 2, "baby");
    peopleMap.replaceAll((k, v) -> {
      if ("Lara".equalsIgnoreCase(k)) {
        return sarah;
      }
      return v;
    });
    Assert.assertEquals(sarah, peopleMap.get("Lara"));
  }

  @Test
  public void testMapPutIfAbsent() {
    Person ethan = new Person("Ethan", 21, "Student");
    peopleMap.putIfAbsent(ethan.getName(), ethan);

  }

}
