package sample.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;

public class ExecutorSample {

    @Test
    public void testVirtualThreadExecutor01() {
        try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture.runAsync(()-> System.out.println("test"), executor);
        }
    }

}
