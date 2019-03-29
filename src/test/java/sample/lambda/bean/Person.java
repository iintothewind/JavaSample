package sample.lambda.bean;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import java.util.Optional;

public class Person implements Comparable<Person> {
  String name;
  Integer age;
  String occupation;

  public Person(String name, Integer age, String occupation) {
    super();
    this.name = name;
    this.age = age;
    this.occupation = occupation;
  }

  // @Override
  // public int hashCode() {
  // final int prime = 31;
  // int result = 1;
  // result = prime * result + ((age == null) ? 0 : age.hashCode());
  // result = prime * result + ((name == null) ? 0 : name.hashCode());
  // result = prime * result + ((occupation == null) ? 0 : occupation.hashCode());
  // return result;
  // }

  public int hashCode() {
    return Objects.hashCode(name, age, occupation);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Person other = (Person) obj;
    if (age == null) {
      if (other.age != null)
        return false;
    } else if (!age.equals(other.age))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (occupation == null) {
      if (other.occupation != null)
        return false;
    } else if (!occupation.equals(other.occupation))
      return false;
    return true;
  }

  @Override
  public int compareTo(Person person) {
    Person p = Optional.of(person).get();
    return ComparisonChain.start().compare(this.name, p.name).compare(this.age, p.age)
      .compare(this.occupation, p.occupation).result();
  }


  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("age", age).add("occupation", occupation).toString();
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return this.age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getOccupation() {
    return this.occupation;
  }

  public void setOccupation(String occupation) {
    this.occupation = occupation;
  }
}
