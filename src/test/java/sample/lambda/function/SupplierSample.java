package sample.lambda.function;

import org.junit.Assert;
import org.junit.Test;
import sample.lambda.bean.Person;

import java.util.function.Supplier;

public class SupplierSample {

  private Person getPersonBySupplier(String name, int age, String occupation) {
    Supplier<Person> personSupplier = () -> new Person(name, age, occupation);
    return personSupplier.get();
  }

  @Test
  public void testGet() {
    Assert.assertEquals(new Person("sarah", 2, "baby"), getPersonBySupplier("sarah", 2, "baby"));
  }
}
