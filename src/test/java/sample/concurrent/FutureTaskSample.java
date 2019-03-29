package sample.concurrent;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import sample.lambda.bean.Person;

import java.util.ArrayList;
import java.util.concurrent.*;


public class FutureTaskSample {
  private final Logger log = LogManager.getLogger(this.getClass().getName());

  private Person studentJohn = null;
  private Person yongPeter = null;
  private Person olderPeter = null;
  private Person boxerJohn = null;
  private ArrayList<Person> list = null;
  private final FutureTask<Optional<Person>> task = new FutureTask<>(new Callable<Optional<Person>>() {
    @Override
    public Optional<Person> call() throws Exception {
      return findPersonByExample(new Person(null, null, "Boxer"));
    }
  });

  @Before
  public void setUp() {
    studentJohn = new Person("John", 20, "Student");
    yongPeter = new Person("Peter", 20, "Student");
    olderPeter = new Person("Peter", 21, "Student");
    boxerJohn = new Person("John", 20, "Boxer");
    list = Lists.newArrayList(studentJohn, yongPeter, olderPeter, boxerJohn);
  }

  private Optional<Person> findPersonByExample(final Person person) {
    Preconditions.checkNotNull(person);
    return FluentIterable.from(list).firstMatch(new Predicate<Person>() {
      @Override
      public boolean apply(Person input) {
        Preconditions.checkNotNull(input);
        Optional<String> possibleName = Optional.fromNullable(person.getName());
        Optional<Integer> possibleAge = Optional.fromNullable(person.getAge());
        Optional<String> possibleOccupation = Optional.fromNullable(person.getOccupation());
        boolean match = possibleName.isPresent() ? Objects.equal(person.getName(), input.getName()) : true;
        match = match && (possibleAge.isPresent() ? Objects.equal(person.getAge(), input.getAge()) : true);
        match = match
          && (possibleOccupation.isPresent() ? Objects.equal(person.getOccupation(),
          input.getOccupation()) : true);

        return match;
      }
    });
  }

  @Test
  public void testFutureTask() {
    Stopwatch watch = Stopwatch.createStarted();
    task.run();
    try {
      this.log.debug("Person == " + task.get().get());
    } catch (InterruptedException | ExecutionException e) {
      this.log.error(e.getMessage());
    }
    watch.stop();
    this.log.info("new Thread time in ms: " + watch.elapsed(TimeUnit.MILLISECONDS));
  }

  @Test
  public void testExecuteService() {
    Stopwatch watch = Stopwatch.createStarted();
    ExecutorService pool = Executors.newCachedThreadPool();
    Future<Optional<Person>> future = pool.submit(new Callable<Optional<Person>>() {
      @Override
      public Optional<Person> call() throws Exception {
        return findPersonByExample(new Person(null, null, "Boxer"));
      }
    });

    try {
      this.log.debug("Person == " + future.get().get());
    } catch (InterruptedException | ExecutionException e) {
      this.log.error(e.getMessage());
    }
    watch.stop();
    this.log.info("ExecutorService time in ms: " + watch.elapsed(TimeUnit.MILLISECONDS));
  }

}
