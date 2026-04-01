package com.rbac.command;

import com.rbac.system.RBACSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.*;

class CommandRegistryAsyncTest {

    private RBACSystem system;
    private CommandParser parser;
    private PrintStream originalOut;
    private ByteArrayOutputStream outBuffer;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
        parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        originalOut = System.out;
        outBuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBuffer));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        system.shutdown();
    }

    @Test
    void reportUsersAsync_shouldPrintReport() {
        parser.executeCommand("report-users-async", new Scanner(""), system);

        boolean printed = waitUntil(() ->
                outBuffer.toString().contains("=== USER ROLE REPORT ==="),
                Duration.ofSeconds(1));

        assertThat(printed).isTrue();
    }

    @Test
    void saveAsync_shouldWriteAuditLog(@TempDir Path tempDir) throws Exception {
        system.getAuditLog().log("TEST_ACTION", "tester", "target", "details");
        waitUntil(() -> system.getAuditLog().getAll().size() == 1, Duration.ofSeconds(1));

        Path file = tempDir.resolve("audit.txt");
        parser.executeCommand("save-async", new Scanner(file.toString()), system);

        boolean saved = waitUntil(() -> {
            if (!Files.exists(file)) return false;
            try {
                return Files.size(file) > 0;
            } catch (Exception e) {
                return false;
            }
        }, Duration.ofSeconds(1));

        assertThat(saved).isTrue();
        assertThat(Files.readString(file)).contains("TEST_ACTION");
    }

    private boolean waitUntil(Check condition, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.ok()) return true;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return condition.ok();
    }

    @FunctionalInterface
    private interface Check {
        boolean ok();
    }
}
