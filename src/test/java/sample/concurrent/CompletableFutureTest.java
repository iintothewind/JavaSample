package sample.concurrent;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.IntStream;

@Slf4j
public class CompletableFutureTest {

    private ExecutorService pool = null;

    public static <T> CompletableFuture<T> retry(@NonNull final CompletableFuture<T> task, @NonNull final Integer retries, @NonNull final Duration duration) {
        return task.handle((result, ex) -> {
            if (Objects.isNull(ex)) {
                return CompletableFuture.completedFuture(result);
            }
            if (retries > 0) {
                return CompletableFuture
                        .supplyAsync(() -> null, CompletableFuture.delayedExecutor(TimeUnit.MILLISECONDS.convert(duration), TimeUnit.MILLISECONDS))
                        .thenCompose(ignored -> retry(task, retries - 1, duration));
            } else {
                throw new CompletionException("out of retries", ex);
            }
        }).thenCompose(Function.identity());
    }

    @BeforeEach
    public void setUp() {
        pool = Executors.newWorkStealingPool();
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

    @AfterEach
    public void tearDown() {
        Optional.ofNullable(pool).ifPresent((p) -> Try.of(() -> p.awaitTermination(5, TimeUnit.SECONDS)));
    }

    public CompletableFuture<String> askObtrudeExceptionally() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("42");
        future.obtrudeValue("52");
        future.obtrudeException(new IllegalStateException("The job is running to an illegal state."));
        return future;
    }

    @Test
    public void testCompleteExceptionally() throws ExecutionException, InterruptedException {
        Assertions.assertThatThrownBy(() -> askCompletedExceptionally().get())
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    public void testSupplyAsync() throws ExecutionException, InterruptedException {
        final CompletableFuture<Long> future = CompletableFuture.supplyAsync(System::currentTimeMillis, this.pool);
        Assertions.assertThat(System.currentTimeMillis()).isCloseTo(future.get(), Offset.offset(100L));
    }

    @Test
    public void testObtrudeExceptionally() throws ExecutionException, InterruptedException {
        Assertions.assertThatThrownBy(() -> askObtrudeExceptionally().get())
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    public void testRunAsync() {
        CompletableFuture.runAsync(() -> System.getenv()
                        .entrySet().stream().filter(entry -> "Path".equalsIgnoreCase(entry.getKey()))
                        .toList()
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
    public void testThenApplyAsync() throws ExecutionException, InterruptedException {
        // ...Async version APIs will be executed asynchronously in different thread pool
        CompletableFuture<LocalDateTime> future = CompletableFuture.supplyAsync(System::currentTimeMillis, this.pool)
                .thenApplyAsync(currentMillis -> Instant.ofEpochMilli(currentMillis).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .thenApplyAsync(localDateTime -> localDateTime.plusDays(365));
        Assertions.assertThat(future.get()).isAfter(LocalDateTime.now());
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
    public void testAcceptEither() {
        // accept the first completed task then consume it
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.throwIfUnchecked(e);
            }
            return "Do";
        }, this.pool).acceptEitherAsync(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Throwables.throwIfUnchecked(e);
            }
            return "Re";
        }, this.pool), System.out::println);
    }

    @Test
    public void testApplyToEither() throws ExecutionException, InterruptedException {
        // accept the first completed task then apply its result in a function
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(100));
            } catch (InterruptedException e) {
                Throwables.throwIfUnchecked(e);
            }
            return "Do";
        }, this.pool).applyToEither(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(100));
            } catch (InterruptedException e) {
                Throwables.throwIfUnchecked(e);
            }
            return "Re";
        }), Function.identity());
        Assertions.assertThat(ImmutableSet.of("Do", "Re")).contains(future.get());
    }

    private CompletableFuture<Void> sing(String syllable) {
        Objects.requireNonNull(syllable);
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(100));
            } catch (InterruptedException e) {
                Throwables.throwIfUnchecked(e);
            }
            this.log.info(syllable);
        });
    }

    @Test
    public void testAllOf() {
        // wait for all completableFutures to be completed
        CompletableFuture.allOf(this.sing("Do"), this.sing("Re"), this.sing("Mi")).join();
    }

    private CompletableFuture<String> chant(String syllable, final int millis) {
        Objects.requireNonNull(syllable);
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(millis);
            } catch (InterruptedException e) {
                Throwables.throwIfUnchecked(e);
            }
            return String.format("%s:%s", syllable, millis);
        });
    }

    @Test
    public void testAnyOf() {
        CompletableFuture.anyOf(this.chant("Do", 7), this.chant("Re", 9), this.chant("Mi", 20));
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

    static <T> CompletableFuture<T> within(CompletableFuture<T> completableFuture, Duration duration) {
        Objects.requireNonNull(completableFuture, "completableFuture is required");
        Objects.requireNonNull(duration, "duration is required");
        final CompletableFuture<T> promiseFailed = CompletableFuture.supplyAsync(() -> {
            throw new CompletionException(new TimeoutException("timeout for future task execution"));
        }, CompletableFuture.delayedExecutor(TimeUnit.MILLISECONDS.convert(duration), TimeUnit.MILLISECONDS));
        return completableFuture.applyToEither(promiseFailed, Function.identity());
    }

    @Test
    public void testCancellableFuture() {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "this is a message";
        }, this.pool);

        final CompletableFuture<String> cancellable1 = within(future, Duration.ofMillis(50));
        final CompletableFuture<String> cancellable2 = within(future, Duration.ofMillis(200));
        cancellable1.whenComplete((s, throwable) -> {
            if (s != null) {
                log.info("cancellable1: {}", s);
            } else {
                log.info(throwable.getMessage());
            }
        });
        cancellable2.whenComplete((s, throwable) -> {
            if (s != null) {
                log.info("cancellable2 {}", s);
            } else {
                log.info(throwable.getMessage());
            }
        });
    }

    @SneakyThrows
    public static CompletableFuture<Integer> dblFuture(final Integer i) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("execute {} start", i);
            if (i > 5) {
                throw new CompletionException(new IllegalArgumentException(String.format("%s is not acceptable", i)));
            }
            Try.run(() -> TimeUnit.SECONDS.sleep(1L));
            return i * 2;
        });
    }

    @Test
    public void testThenAcceptBoth() {
        // consume (A, B) in BiConsumer after both A and B completed
        CompletableFuture.supplyAsync(() -> ImmutableMap.of("name", "Sid"), this.pool)
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> "name"),
                        (map, key) -> Assertions.assertThat(map.get(key)).isEqualTo("Sid"));
    }

    @Test
    public void testReduceFutures01() {
        final Future<Integer> reduced = Future.reduce(
                IntStream.range(1, 9)
                        .mapToObj(i ->
                                Future.fromCompletableFuture(dblFuture(i)
                                        .handle((n, t) -> {
                                            if (Objects.nonNull(t)) {
                                                log.error("failed to execute async service: {}", t.getMessage());
                                                return 0;
                                            } else {
                                                log.info("input: {} , result: {}", i, n);
                                                return n;
                                            }
                                        })
                                )
                        )
                        .toList(),
                (l, r) -> l + r
        );

        final String result = String.valueOf(reduced.getOrElse(-1));
        log.info("result: {}", result);
    }

    @Test
    public void testRetry01() {
        final CompletableFuture<Integer> future = retry(CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("error");
        }), 5, Duration.ofMillis(100));
        final Integer result = Future.fromCompletableFuture(future).getOrElse(-1);
        log.info("result: {}", result);
    }

}
