package sample.basic;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import sample.lambda.bean.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LambdaTest {

  @Test
  public void runnableLambda() {
    Runnable anonymousRun = new Runnable() {
      @Override
      public void run() {
        System.out.println("hello, impl by anonymous runnable.");
      }
    };

    Runnable lambdaRun = () -> System.out.println("hello, impl by lambda");

    anonymousRun.run();
    lambdaRun.run();
  }

  @Test
  public void comparatorLambda() {
    List<Person> people = Lists.newArrayList(new Person("Ethan", 21, "Student"), new Person("Mars", 22, "Detect"), new Person("Lara", 20, "Fighter"));
    Collections.sort(people, new Comparator<Person>() {
      @Override
      public int compare(Person left, Person right) {
        return left.compareTo(right);
      }
    });
    people.forEach(person -> System.out.println(person.toString()));
    people.sort(Person::compareTo);
    people.forEach(person -> System.out.println(person.toString()));
  }

  @Test
  public void listenerLambda() {
    JButton button = new JButton("Test Button");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("click detected by anon class");
      }
    });

    button.addActionListener(e -> System.out.println("click detected by lambda listener"));
    JFrame frame = new JFrame("Listener Test");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.add(button, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }
}
