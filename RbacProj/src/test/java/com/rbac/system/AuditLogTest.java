package com.rbac.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @AfterEach
    void tearDown() {
        auditLog.shutdown();
    }

    @Test
    void log_shouldBeProcessedByBackgroundWorker() {
        auditLog.log("TEST_ACTION", "tester", "target", "details");

        boolean processed = waitUntil(() -> auditLog.getAll().size() == 1, Duration.ofSeconds(1));

        assertThat(processed).isTrue();
        assertThat(auditLog.getByPerformer("tester")).hasSize(1);
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
