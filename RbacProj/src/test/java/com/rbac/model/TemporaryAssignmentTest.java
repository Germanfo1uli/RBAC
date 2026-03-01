package com.rbac.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class TemporaryAssignmentTest {

    private User user;
    private Role role;
    private AssignmentMetadata meta;
    private String futureDate;
    private String pastDate;

    @BeforeEach
    void setUp() {
        user = User.validate("bob", "Bob", "bob@ex.com");
        role = new Role("Viewer", "View only");
        meta = AssignmentMetadata.now("admin");
        futureDate = LocalDateTime.now().plusDays(10).format(TemporaryAssignment.FORMATTER);
        pastDate = LocalDateTime.now().minusDays(1).format(TemporaryAssignment.FORMATTER);
    }

    @Test
    void shouldBeActiveWhenExpirationInFuture() {
        TemporaryAssignment a = new TemporaryAssignment(user, role, meta, futureDate, false);
        assertThat(a.isActive()).isTrue();
    }

    @Test
    void shouldBeInactiveWhenExpired() {
        TemporaryAssignment a = new TemporaryAssignment(user, role, meta, pastDate, false);
        assertThat(a.isActive()).isFalse();
        assertThat(a.isExpired()).isTrue();
    }

    @Test
    void shouldExtendExpiration() {
        TemporaryAssignment a = new TemporaryAssignment(user, role, meta, futureDate, false);
        String newDate = LocalDateTime.now().plusDays(30).format(TemporaryAssignment.FORMATTER);
        a.extend(newDate);
        assertThat(a.getExpiresAt()).isEqualTo(newDate);
    }
}