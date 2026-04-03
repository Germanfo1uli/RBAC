package com.rbac.test;

import com.rbac.filter.assignment.AssignmentFilters;
import com.rbac.filter.role.RoleFilters;
import com.rbac.filter.user.UserFilters;
import com.rbac.model.AssignmentMetadata;
import com.rbac.model.PermanentAssignment;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.system.RBACSystem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RBACLoadTest {

    @Test
    void shouldHandleConcurrentOperationsWithoutErrorsOrInconsistencies() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();

        final int numThreads = 10;
        final int operationsPerThread = 20;

        AtomicBoolean hadError = new AtomicBoolean(false);
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int op = 0; op < operationsPerThread; op++) {
                    try {
                        String username = "loaduser_" + threadId + "_" + op;
                        User user = User.validate(username,
                                "Load Test User " + threadId,
                                username + "@loadtest.com");

                        try {
                            system.getUserManager().add(user);
                        } catch (IllegalArgumentException e) {
                            if (!e.getMessage().contains("already exists")) {
                                throw e;
                            }
                        }

                        system.getUserManager().update(username,
                                "Updated Load User " + threadId,
                                username + "@updated.com");

                        String roleName = "loadrole" + (threadId % 5);
                        Role role = null;

                        if (!system.getRoleManager().exists(roleName)) {
                            try {
                                role = new Role(roleName, "Concurrent load test role");
                                system.getRoleManager().add(role);
                            } catch (IllegalArgumentException e) {
                                if (!e.getMessage().contains("already exists")) {
                                    throw e;
                                }
                            }
                        }

                        if (role == null) {
                            role = system.getRoleManager().findByName(roleName).orElse(null);
                        }

                        if (role != null) {
                            User currentUser = system.getUserManager().findByUsername(username).orElse(null);
                            if (currentUser != null) {
                                AssignmentMetadata meta = AssignmentMetadata.now("loadtester");
                                PermanentAssignment assignment = new PermanentAssignment(currentUser, role, meta);

                                try {
                                    system.getAssignmentManager().add(assignment);
                                } catch (IllegalStateException ignored) {
                                }
                            }
                        }

                        system.getUserManager().findByFilter(
                                UserFilters.byUsernameContains("loaduser"));

                        system.getRoleManager().findByFilter(
                                RoleFilters.byNameContains("loadrole"));

                        system.getAssignmentManager().findByFilter(
                                AssignmentFilters.activeOnly());

                        system.getAssignmentManager().getUserPermissions(user);

                    } catch (Exception e) {
                        hadError.set(true);
                        System.err.println("Thread " + threadId + " error at op " + op + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        assertThat(hadError.get())
                .as("No unhandled exceptions should occur during concurrent operations")
                .isFalse();

        List<User> allUsers = system.getUserManager().findAll();
        Set<String> uniqueUsernames = allUsers.stream()
                .map(User::username)
                .collect(Collectors.toSet());
        assertThat(uniqueUsernames.size())
                .as("All usernames must be unique")
                .isEqualTo(allUsers.size());

        List<Role> allRoles = system.getRoleManager().findAll();
        Set<String> uniqueRoleNames = allRoles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        assertThat(uniqueRoleNames.size())
                .as("All role names must be unique")
                .isEqualTo(allRoles.size());

        System.out.println("Load test completed successfully:");
        System.out.println("  Users created: " + allUsers.size());
        System.out.println("  Roles created: " + allRoles.size());
        System.out.println("  Assignments: " + system.getAssignmentManager().count());

        system.shutdown();
    }
}
