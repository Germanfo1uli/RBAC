package com.rbac.manager;

import com.rbac.model.Permission;
import com.rbac.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RoleManagerTest {

    private RoleManager manager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        assignmentManager = new AssignmentManager();
        manager = new RoleManager(assignmentManager);
    }

    @Test
    void shouldAddAndFindRoleByName() {
        Role r = new Role("Admin", "Full access");
        manager.add(r);

        assertThat(manager.findByName("Admin")).isPresent();
        assertThat(manager.exists("Admin")).isTrue();
    }

    @Test
    void shouldAddAndRemovePermissionFromRole() {
        Role r = new Role("Editor", "Can edit");
        manager.add(r);

        Permission p = new Permission("WRITE", "articles", "Write articles");
        manager.addPermissionToRole("Editor", p);

        assertThat(manager.findByName("Editor").orElseThrow().getPermissions()).contains(p);

        manager.removePermissionFromRole("Editor", p);

        assertThat(manager.findByName("Editor").orElseThrow().getPermissions()).doesNotContain(p);
    }
}