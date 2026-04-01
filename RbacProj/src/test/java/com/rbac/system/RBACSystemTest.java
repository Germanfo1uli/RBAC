package com.rbac.system;

import com.rbac.model.Role;
import com.rbac.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
    }

    @AfterEach
    void tearDown() {
        system.getBackgroundExecutor().shutdown();
    }

    @Test
    void initialize_shouldCreateAdminAndThreeRoles() {
        assertThat(system.getUserManager().count()).isEqualTo(1);
        assertThat(system.getRoleManager().count()).isEqualTo(3);
        assertThat(system.getAssignmentManager().count()).isEqualTo(1);

        assertThat(system.getCurrentUser()).isEqualTo("admin");

        User admin = system.getUserManager().findByUsername("admin").orElseThrow();
        Role adminRole = system.getRoleManager().findByName("Admin").orElseThrow();

        assertThat(system.getAssignmentManager().userHasRole(admin, adminRole)).isTrue();
    }

    @Test
    void generateStatistics_shouldContainCorrectNumbers() {
        String stats = system.generateStatistics();

        assertThat(stats).contains("Users:1");
        assertThat(stats).contains("Roles:3");
        assertThat(stats).contains("Total role assignments:1");
        assertThat(stats).contains("Active assignments:1");
        assertThat(stats).contains("Expired / revoked assignments:0");
        assertThat(stats).contains("Current administrator: admin");
    }

    @Test
    void setCurrentUser_shouldTrimAndHandleNull() {
        system.setCurrentUser("   bob   ");
        assertThat(system.getCurrentUser()).isEqualTo("bob");

        system.setCurrentUser("");
        assertThat(system.getCurrentUser()).isNull();

        system.setCurrentUser(null);
        assertThat(system.getCurrentUser()).isNull();
    }

    @Test
    void backgroundExecutor_shouldRunTasks() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        system.getBackgroundExecutor().submit(latch::countDown);

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
    }
}
