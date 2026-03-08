package com.rbac.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void shouldLogActionAndRetrieveIt() {
        auditLog.log("CREATE_USER", "admin", "user1", "Initial creation");

        var entries = auditLog.getAll();
        assertEquals(1, entries.size());
        assertEquals("CREATE_USER", entries.get(0).action());
    }

    @Test
    void shouldFilterByPerformer() {
        auditLog.log("ACTION1", "admin", "target", "details");
        auditLog.log("ACTION2", "operator", "target", "details");

        assertEquals(1, auditLog.getByPerformer("admin").size());
    }
}