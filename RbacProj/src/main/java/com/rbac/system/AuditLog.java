package com.rbac.system;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AuditLog implements AutoCloseable {

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {
        @Override
        public String toString() {
            return String.format("[%s] %s | Performer: %s | Target: %s | Info: %s",
                    timestamp, action, performer, target, details);
        }
    }

    private final List<AuditEntry> entries = new CopyOnWriteArrayList<>();
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread worker;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLog() {
        worker = new Thread(this::processQueue, "audit-log-worker");
        worker.setDaemon(true);
        worker.start();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        enqueue(new AuditEntry(timestamp, action, performer, target, details));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        System.out.println("=== SYSTEM AUDIT LOG ===");
        entries.forEach(System.out::println);
        System.out.println("========================");
    }

    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) {
                writer.println(entry.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            worker.interrupt();
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    private void enqueue(AuditEntry entry) {
        if (!running.get()) {
            entries.add(entry);
            return;
        }
        queue.offer(entry);
    }

    private void processQueue() {
        try {
            while (running.get() || !queue.isEmpty()) {
                AuditEntry entry = queue.poll(200, TimeUnit.MILLISECONDS);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            while (!queue.isEmpty()) {
                AuditEntry entry = queue.poll();
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
    }
}
