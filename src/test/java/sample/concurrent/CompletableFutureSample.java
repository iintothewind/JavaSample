package sample.concurrent;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompletableFutureSample {
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    final Random random = new Random();
    private ExecutorService pool = null;

    @Before
    public void setUp() {
        this.pool = Executors.newWorkStealingPool();
    }

    @After
    public void tearDown() throws InterruptedException {
        this.pool.awaitTermination(3, TimeUnit.SECONDS);
        this.pool.shutdownNow();
    }

    public CompletableFuture<String> askIncompleted() {
        CompletableFuture<String> future = new CompletableFuture<>();

        return future;
    }

    @Test
    public void testGetNow() {
        Assertions.assertThat("0").isEqualTo(askIncompleted().getNow("0"));
    }

    public CompletableFuture<String> ask() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("42"); // remove this line, ask().get() will be blocked forever
        future.complete("53"); // invalid call, result will not be changed. CompletableFuture.complete() can only be called once
        future.obtrudeValue("52"); // CompletableFuture.obtrudeValue() can override the value of the CompletableFuture
        future.obtrudeValue("51"); // This method is designed for use only in error recovery actions
        return future;
    }

    @Test
    public void testSimplyCreatedCompletableFuture() throws ExecutionException, InterruptedException {
        Assertions.assertThat("51").isEqualTo(ask().get());
        Assertions.assertThat("51").isEqualTo(ask().join());
    }

    public CompletableFuture<String> askCompletedExceptionally() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new IllegalStateException("The job is running to an illegal state."));
        return future;
    }

    @Test(expected = ExecutionException.class)
    public void testCompleteExceptionally() throws ExecutionException, InterruptedException {
        askCompletedExceptionally().get();
    }

    public CompletableFuture<String> askObtrudeExceptionally() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("42");
        future.obtrudeValue("52");
        future.obtrudeException(new IllegalStateException("The job is running to an illegal state."));
        return future;
    }

    @Test(expected = ExecutionException.class)
    public void testObtrudeExceptionally() throws ExecutionException, InterruptedException {
        askObtrudeExceptionally().get();
    }

    @Test
    public void testSupplyAsync() throws ExecutionException, InterruptedException {
        final CompletableFuture<Long> future = CompletableFuture.supplyAsync(System::currentTimeMillis, this.pool);
        Assertions.assertThat(System.currentTimeMillis()).isCloseTo(future.get(), Offset.offset(100L));
    }

    @Test
    public void testRunAsync() throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(() -> System.getenv()
                .entrySet().stream().filter(entry -> "Path".equalsIgnoreCase(entry.getKey()))
                .collect(Collectors.toList())
                .forEach(entry -> System.out.println(String.format("%s = %s", entry.getKey(), entry.getValue()))), this.pool)
                .thenRun(() -> System.out.println("Done"));
    }

    @Test
    public void testThenApply() throws ExecutionException, InterruptedException {
        // non ...Async version APIs will be executed asynchronously in same thread
        CompletableFuture<String> future = CompletableFuture.supplyAsync(System::currentTimeMillis, this.pool)
                .thenApply(currentMillis -> Instant.ofEpochMilli(currentMillis).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .thenApply(localDateTime -> localDateTime.plusDays(365))
                .thenApply((localDateTime) -> localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Assertions.assertThat(LocalDateTime.parse(future.get(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).isAfter(LocalDateTime.now());
    }

    @Test
    public void testThenApplyAsync() throws ExecutionException, InterruptedException {
        // ...Async version APIs will be executed asynchronously in different thread pool
        CompletableFuture<LocalDateTime> future = CompletableFuture.supplyAsync(System::currentTimeMillis, this.pool)
                .thenApplyAsync(currentMillis -> Instant.ofEpochMilli(currentMillis).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .thenApplyAsync(localDateTime -> localDateTime.plusDays(365));
        Assertions.assertThat(future.get()).isAfter(LocalDateTime.now());
    }

    @Test
    public void testErrorHandleExceptionally() throws ExecutionException, InterruptedException {
        //exceptionally() takes a function that will be invoked when original future throws an exception.
        //We then have an opportunity to recover by transforming this exception into some value compatible with Future's type.
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> String.valueOf(1 / 0), this.pool).exceptionally(Throwable::getMessage);
        Assertions.assertThat(future.get()).isEqualTo("java.lang.ArithmeticException: / by zero");
    }

    @Test
    public void testHandleCorrectness() throws InterruptedException, ExecutionException {
        // BiConsumer in .hanlder() receives either val or exception
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> String.valueOf(1), this.pool).handle((val, ex) -> {
            if (Objects.nonNull(val)) {
                return "handled:".concat(val);
            } else {
                return ex.getMessage();
            }
        });
        Assertions.assertThat(future.get()).contains("handled");
    }

    @Test
    public void testHandleException() throws InterruptedException, ExecutionException {
        // BiConsumer in .hanlder() receives either val or exception
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> String.valueOf(1 / 0), this.pool).handle((val, ex) -> {
            if (Objects.nonNull(val)) {
                return "handled:".concat(val);
            } else {
                return ex.getMessage();
            }
        });
        Assertions.assertThat(future.get()).isEqualTo("java.lang.ArithmeticException: / by zero");
    }

    @Test
    public void testThenCompose() throws ExecutionException, InterruptedException {
        // .thenCompose() is used to chain one future dependent on the other
        // .thenCompose() should be used when using the result of first CompletableFuture to convert to another CompletableFuture.
        // e.g. when using thenApply() it should return a CompletableFuture<CompletableFuture<T>> and flatMap to CompletableFuture<U>
        CompletableFuture<Optional<Map.Entry<String, String>>> future = CompletableFuture.supplyAsync(() -> System.getenv().entrySet(), this.pool)
                .thenCompose(entries -> CompletableFuture.supplyAsync(() -> entries.stream().filter(entry -> "Path".equalsIgnoreCase(entry.getKey())).findAny()));
        Assertions.assertThat(future.get().isPresent()).isTrue();
    }

    @Test
    public void testThenCombine() throws ExecutionException, InterruptedException {
        // .thenCombine() combines two independent futures and converts to final result
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> "1", this.pool)
                .thenCombine(CompletableFuture.supplyAsync(() -> "2"), (left, right) -> Integer.parseInt(left + right));
        Assertions.assertThat(future.get()).isEqualTo(12);
    }

    @Test
    public void testThenAcceptBoth() {
        CompletableFuture.supplyAsync(() -> ImmutableMap.of("name", "Sid"), this.pool)
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> "name"), (map, key) -> Assertions.assertThat(map.get(key)).isEqualTo("Sids"))
                .handle((v, e) -> {
                    if (Objects.nonNull(e)) {
                        System.out.println(e.getMessage());
                    }
                    return v;
                });
    }

    @Test
    public void testAcceptEither() {
        Random random = new Random();
        CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
            return "Do";
        }, this.pool).acceptEitherAsync(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
            return "Re";
        }), System.out::println);
    }

    @Test
    public void testApplyToEither() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
            return "Do";
        }, this.pool).applyToEither(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
            return "Re";
        }), Function.identity());
        Assertions.assertThat(ImmutableSet.of("Do", "Re")).contains(future.get());
    }

    private CompletableFuture<Void> sing(String syllable) {
        Objects.requireNonNull(syllable);
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
            this.log.info(syllable);
        });
    }

    @Test
    public void testAllOf() {
        CompletableFuture.allOf(this.sing("Do"), this.sing("Re"), this.sing("Mi")).join();
    }

    private CompletableFuture<String> chant(String syllable) {
        Objects.requireNonNull(syllable);
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
            return syllable;
        });
    }

    @Test
    public void testAnyOf() {
        CompletableFuture.anyOf(this.chant("Do"), this.chant("Re"), this.chant("Mi")).thenAccept(this.log::info);
    }

    @Test
    public void testLocalDatetime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String threeMonthsLater = LocalDateTime.now().plus(1, IsoFields.QUARTER_YEARS).format(format);
        LocalDateTime.parse(threeMonthsLater, format);
    }


    @Test
    public void testParallel() {
        System.out.println("ForkJoinPool.getCommonPoolParallelism() == " + ForkJoinPool.getCommonPoolParallelism());
    }

}
