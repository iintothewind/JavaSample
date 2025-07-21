package sample.concurrent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorTest {

    @Test
    public void testVirtualThreadExecutor01() {
        try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture.runAsync(()-> System.out.println("test"), executor);
        }
    }

}
