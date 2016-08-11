package sample.blocker.config;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Config {
    private final Queue<Blocker> blockers = new LinkedBlockingQueue<>();

    public Queue<Blocker> getBlockers() {
        return blockers;
    }

    @JsonAnySetter
    public void addBlocker(String key, Blocker blocker) {
        blockers.add(blocker);
    }
}
