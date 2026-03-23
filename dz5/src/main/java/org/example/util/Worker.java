package org.example.util;

import java.util.Random;

public class Worker implements Runnable {
    private final ThreadState state;
    private final Random random;

    public Worker(ThreadState state) {
        this.state = state;
        this.random = new Random();
    }

    @Override
    public void run() {
        state.start(Thread.currentThread().getId());
        try {
            for (int i = 0; i < state.getMaxProgress(); i++) {
                Thread.sleep(50 + random.nextInt(150));
                state.incrementProgress();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            state.finish();
        }
    }
}