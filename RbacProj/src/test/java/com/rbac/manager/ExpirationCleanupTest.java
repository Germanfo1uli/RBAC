package com.rbac.manager;

import com.rbac.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ExpirationCleanupTest {

    private AssignmentManager manager;
    private User user1;
    private User user2;
    private Role tempRole;
    private Role permRole;
    private AssignmentMetadata meta;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
        user1 = User.validate("user1", "Test User One", "user1@ex.com");
        user2 = User.validate("user2", "Test User Two", "user2@ex.com");
        tempRole = new Role("TempRole", "Temporary test role");
        permRole = new Role("PermRole", "Permanent test role");
        meta = AssignmentMetadata.now("system");
    }

    @AfterEach
    void tearDown() {
        manager.clear();
    }

    @Test
    void shouldRemoveOnlyExpiredTemporaryAssignments() {
        String future = LocalDateTime.now().plusHours(1).format(TemporaryAssignment.FORMATTER);
        String past = LocalDateTime.now().minusHours(1).format(TemporaryAssignment.FORMATTER);

        TemporaryAssignment activeTemp = new TemporaryAssignment(user1, tempRole, meta, future, false);
        TemporaryAssignment expiredTemp = new TemporaryAssignment(user2, tempRole, meta, past, false);
        PermanentAssignment permanent = new PermanentAssignment(user1, permRole, meta);

        manager.add(activeTemp);
        manager.add(expiredTemp);
        manager.add(permanent);

        int cleaned = manager.cleanupExpiredTemporaryAssignments();

        assertThat(cleaned).isEqualTo(1);
        assertThat(manager.count()).isEqualTo(2);
        assertThat(manager.findById(expiredTemp.assignmentId())).isEmpty();
        assertThat(manager.findById(activeTemp.assignmentId())).isPresent();
        assertThat(manager.findById(permanent.assignmentId())).isPresent();
    }

    @Test
    void shouldNotRemoveActiveTemporaryAssignments() {
        String future = LocalDateTime.now().plusHours(2).format(TemporaryAssignment.FORMATTER);
        TemporaryAssignment active = new TemporaryAssignment(user1, tempRole, meta, future, true);

        manager.add(active);

        int cleaned = manager.cleanupExpiredTemporaryAssignments();

        assertThat(cleaned).isZero();
        assertThat(manager.count()).isEqualTo(1);
    }

    @Test
    void shouldHandleEmptyAssignmentsGracefully() {
        int cleaned = manager.cleanupExpiredTemporaryAssignments();
        assertThat(cleaned).isZero();
    }
}
