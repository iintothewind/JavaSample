package sample.concurrent;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import sample.lambda.bean.Person;


public class FutureTaskSample {
    private final Logger log = LogManager.getLogger(this.getClass().getName());

    private Person studentJohn = null;
    private Person yongPeter = null;
    private Person olderPeter = null;
    private Person boxerJohn = null;
    private ArrayList<Person> list = null;

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

    private final FutureTask<Optional<Person>> task = new FutureTask<>(new Callable<Optional<Person>>() {
        @Override
        public Optional<Person> call() throws Exception {
            return findPersonByExample(new Person(null, null, "Boxer"));
        }
    });

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
