package com.rbac.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PermanentAssignmentTest {

    private User user;
    private Role role;
    private AssignmentMetadata meta;

    @BeforeEach
    void setUp() {
        user = User.validate("alice", "Alice", "alice@ex.com");
        role = new Role("Dev", "Developer");
        meta = AssignmentMetadata.now("admin");
    }

    @Test
    void shouldBeActiveByDefault() {
        PermanentAssignment a = new PermanentAssignment(user, role, meta);
        assertThat(a.isActive()).isTrue();
        assertThat(a.assignmentType()).isEqualTo("PERMANENT");
    }

    @Test
    void shouldBecomeInactiveAfterRevoke() {
        PermanentAssignment a = new PermanentAssignment(user, role, meta);
        a.revoke();
        assertThat(a.isActive()).isFalse();
        assertThat(a.isRevoked()).isTrue();
    }
}