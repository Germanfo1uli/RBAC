package com.rbac.manager;

import com.rbac.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class AssignmentManagerTest {

    private AssignmentManager manager;
    private User user;
    private Role roleA;
    private Role roleB;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
        user = User.validate("alice", "Alice", "alice@ex.com");
        roleA = new Role("Dev", "Developer");
        roleB = new Role("QA", "Tester");

        roleA.addPermission(new Permission("WRITE", "code", "Write code"));
        roleB.addPermission(new Permission("READ", "tests", "Read tests"));
    }

    @Test
    void shouldAddPermanentAssignmentAndCheckActive() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin");
        PermanentAssignment a = new PermanentAssignment(user, roleA, meta);

        manager.add(a);

        assertThat(manager.userHasRole(user, roleA)).isTrue();
        assertThat(manager.getActiveAssignments()).hasSize(1);
    }

    @Test
    void shouldCollectPermissionsFromMultipleRoles() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin");
        manager.add(new PermanentAssignment(user, roleA, meta));
        manager.add(new PermanentAssignment(user, roleB, meta));

        Set<Permission> perms = manager.getUserPermissions(user);

        assertThat(perms).extracting(Permission::name).containsExactlyInAnyOrder("WRITE", "READ");
    }

    @Test
    void shouldRevokePermanentAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin");
        PermanentAssignment a = new PermanentAssignment(user, roleA, meta);
        manager.add(a);

        manager.revokeAssignment(a.assignmentId());

        assertThat(manager.userHasRole(user, roleA)).isFalse();
        assertThat(manager.getActiveAssignments()).isEmpty();
    }
}