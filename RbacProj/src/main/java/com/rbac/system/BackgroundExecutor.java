package com.rbac.system;

import java.util.Objects;
import java.util.concurrent.*;

public class BackgroundExecutor implements AutoCloseable {
    private final ExecutorService executor;

    public BackgroundExecutor() {
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public Future<?> submit(Runnable task) {
        Objects.requireNonNull(task, "task");
        return executor.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        Objects.requireNonNull(task, "task");
        return executor.submit(task);
    }

    public void execute(Runnable task) {
        Objects.requireNonNull(task, "task");
        executor.execute(task);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public void shutdownNow() {
        executor.shutdownNow();
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    @Override
    public void close() {
        shutdown();
    }
}
