package sample.lambda.function;

import sample.lambda.bean.Person;
import org.junit.Test;

import java.util.function.Predicate;

public class PredicateSample {
    private void driveTest(Person person, Predicate<Person> predicate) {
        if (predicate.test(person)) {
            System.out.println(person.getName() + " passed drive test.");
        } else {
            System.out.println(person.getName() + " did not pass drive test.");
        }
    }

    @Test
    public void testDrive() {
        Person lara = new Person("Lara", 18, "student");
        Person jim = new Person("jim", 21, "journalist");
        Person zoe = new Person("zoe", 22, "driver");
        Predicate<Person> ageRule = person -> person.getAge() > 20;
        Predicate<Person> occupationRule = person -> "driver".equalsIgnoreCase(person.getOccupation());

        this.driveTest(lara, ageRule);
        this.driveTest(jim, ageRule.and(occupationRule));
        this.driveTest(zoe, ageRule.and(occupationRule));
    }
}
