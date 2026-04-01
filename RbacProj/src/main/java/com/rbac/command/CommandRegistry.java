package com.rbac.command;

import com.rbac.filter.user.UserFilter;
import com.rbac.filter.user.UserFilters;
import com.rbac.model.*;
import com.rbac.report.ReportGenerator;
import com.rbac.system.RBACSystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser) {

        parser.registerCommand("user-list", "List all users (with optional filters)", (sc, sys) -> {
            System.out.println("\nUsers list:");
            List<User> users = sys.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }
            System.out.printf("%-15s %-25s %-30s%n", "Username", "Full Name", "Email");
            System.out.println("-".repeat(70));
            users.forEach(u -> System.out.printf("%-15s %-25s %-30s%n", u.username(), u.fullName(), u.email()));
        });

        parser.registerCommand("user-create", "Create new user", (sc, sys) -> {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            System.out.print("Enter full name: ");
            String fullName = sc.nextLine().trim();

            System.out.print("Enter email: ");
            String email = sc.nextLine().trim();

            try {
                User user = User.validate(username, fullName, email);
                sys.getUserManager().add(user);
                System.out.println("User created successfully: " + user.format());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "View user details", (sc, sys) -> {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            Optional<User> opt = sys.getUserManager().findByUsername(username);
            if (opt.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            User user = opt.get();
            System.out.println("\nUser: " + user.format());

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("No roles assigned.");
            } else {
                System.out.println("Assigned roles:");
                assignments.forEach(a -> {
                    System.out.println("  • " + a.role().getName() + " (" + a.assignmentType() + ") " +
                            (a.isActive() ? "[ACTIVE]" : "[INACTIVE]") + " since " + a.metadata().assignedAt());
                });
            }

            Set<Permission> perms = sys.getAssignmentManager().getUserPermissions(user);
            if (!perms.isEmpty()) {
                System.out.println("\nPermissions:");
                perms.forEach(p -> System.out.println("  • " + p.format()));
            }
        });

        parser.registerCommand("user-update", "Update user details", (sc, sys) -> {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            if (!sys.getUserManager().exists(username)) {
                System.out.println("User not found.");
                return;
            }

            System.out.print("New full name (enter to skip): ");
            String newFullName = sc.nextLine().trim();
            if (newFullName.isEmpty()) newFullName = null;

            System.out.print("New email (enter to skip): ");
            String newEmail = sc.nextLine().trim();
            if (newEmail.isEmpty()) newEmail = null;

            try {
                User current = sys.getUserManager().findByUsername(username).get();
                String updatedName = newFullName != null ? newFullName : current.fullName();
                String updatedEmail = newEmail != null ? newEmail : current.email();

                sys.getUserManager().update(username, updatedName, updatedEmail);
                System.out.println("User updated successfully.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Delete user", (sc, sys) -> {
            System.out.print("Enter username to delete: ");
            String username = sc.nextLine().trim();

            if (!sys.getUserManager().exists(username)) {
                System.out.println("User not found.");
                return;
            }

            System.out.print("Are you sure? Type 'yes' to confirm: ");
            if (!sc.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            User user = sys.getUserManager().findByUsername(username).get();
            sys.getAssignmentManager().findByUser(user).forEach(a -> sys.getAssignmentManager().revokeAssignment(a.assignmentId()));
            sys.getUserManager().remove(user);
            System.out.println("User and all assignments deleted.");
        });

        parser.registerCommand("user-search", "Search users by filter", (sc, sys) -> {
            System.out.println("Search by:");
            System.out.println("  1. Username contains");
            System.out.println("  2. Email contains");
            System.out.println("  3. Email domain");
            System.out.println("  4. Full name contains");
            System.out.print("Choose (1-4): ");

            String choice = sc.nextLine().trim();
            UserFilter filter = null;

            switch (choice) {
                case "1" -> {
                    System.out.print("Substring in username: ");
                    filter = UserFilters.byUsernameContains(sc.nextLine().trim());
                }
                case "2" -> {
                    System.out.print("Substring in email: ");
                    filter = u -> u.email().toLowerCase().contains(sc.nextLine().trim().toLowerCase());
                }
                case "3" -> {
                    System.out.print("Email domain (e.g. @company.com): ");
                    filter = UserFilters.byEmailDomain(sc.nextLine().trim());
                }
                case "4" -> {
                    System.out.print("Substring in full name: ");
                    filter = UserFilters.byFullNameContains(sc.nextLine().trim());
                }
                default -> {
                    System.out.println("Invalid choice.");
                    return;
                }
            }

            List<User> results = sys.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("\nFound " + results.size() + " users:");
                results.forEach(u -> System.out.println("  " + u.format()));
            }
        });

        parser.registerCommand("role-list", "List all roles", (sc, sys) -> {
            List<Role> roles = sys.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles found.");
                return;
            }
            System.out.printf("%-20s %-8s %s%n", "Name", "Perms", "ID");
            System.out.println("-".repeat(60));
            roles.forEach(r -> System.out.printf("%-20s %-8d %s%n", r.getName(), r.getPermissionCount(), r.getId()));
        });

        parser.registerCommand("role-create", "Create new role", (sc, sys) -> {
            System.out.print("Role name: ");
            String name = sc.nextLine().trim();

            if (sys.getRoleManager().exists(name)) {
                System.out.println("Role with this name already exists.");
                return;
            }

            System.out.print("Description: ");
            String desc = sc.nextLine().trim();

            Role role = new Role(name, desc);
            sys.getRoleManager().add(role);
            System.out.println("Role created: " + name);

            while (true) {
                System.out.print("Add permission? (y/n): ");
                if (!sc.nextLine().trim().equalsIgnoreCase("y")) break;

                System.out.print("Permission name (UPPERCASE): ");
                String pName = sc.nextLine().trim().toUpperCase();

                System.out.print("Resource (lowercase): ");
                String resource = sc.nextLine().trim().toLowerCase();

                System.out.print("Description: ");
                String pDesc = sc.nextLine().trim();

                try {
                    Permission perm = new Permission(pName, resource, pDesc);
                    sys.getRoleManager().addPermissionToRole(name, perm);
                    System.out.println("Permission added.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });

        parser.registerCommand("role-view", "View role details", (sc, sys) -> {
            System.out.print("Enter role name: ");
            String name = sc.nextLine().trim();

            Optional<Role> opt = sys.getRoleManager().findByName(name);
            if (opt.isEmpty()) {
                System.out.println("Role not found.");
                return;
            }
            System.out.println(opt.get().format());
        });

        parser.registerCommand("role-delete", "Delete role", (sc, sys) -> {
            System.out.print("Enter role name to delete: ");
            String name = sc.nextLine().trim();

            Optional<Role> opt = sys.getRoleManager().findByName(name);
            if (opt.isEmpty()) {
                System.out.println("Role not found.");
                return;
            }

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(opt.get());
            if (!assignments.isEmpty()) {
                System.out.println("Warning: role is assigned to " + assignments.size() + " users:");
                assignments.forEach(a -> System.out.println("  • " + a.user().username()));
                System.out.print("Delete anyway? (yes/no): ");
                if (!sc.nextLine().trim().equalsIgnoreCase("yes")) {
                    System.out.println("Cancelled.");
                    return;
                }
            }

            sys.getRoleManager().remove(opt.get());
            System.out.println("Role deleted.");
        });

        parser.registerCommand("role-add-permission", "Add permission to role", (sc, sys) -> {
            System.out.print("Role name: ");
            String roleName = sc.nextLine().trim();

            if (!sys.getRoleManager().exists(roleName)) {
                System.out.println("Role not found.");
                return;
            }

            System.out.print("Permission name (UPPERCASE): ");
            String pName = sc.nextLine().trim().toUpperCase();

            System.out.print("Resource (lowercase): ");
            String resource = sc.nextLine().trim().toLowerCase();

            System.out.print("Description: ");
            String desc = sc.nextLine().trim();

            try {
                Permission perm = new Permission(pName, resource, desc);
                sys.getRoleManager().addPermissionToRole(roleName, perm);
                System.out.println("Permission added to role.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Remove permission from role", (sc, sys) -> {
            System.out.print("Role name: ");
            String roleName = sc.nextLine().trim();

            Optional<Role> opt = sys.getRoleManager().findByName(roleName);
            if (opt.isEmpty()) {
                System.out.println("Role not found.");
                return;
            }

            Role role = opt.get();
            List<Permission> perms = new ArrayList<>(role.getPermissions());
            if (perms.isEmpty()) {
                System.out.println("Role has no permissions.");
                return;
            }

            System.out.println("Permissions:");
            for (int i = 0; i < perms.size(); i++) {
                System.out.printf("%2d) %s%n", i + 1, perms.get(i).format());
            }

            System.out.print("Enter number to remove: ");
            try {
                int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
                if (idx < 0 || idx >= perms.size()) {
                    System.out.println("Invalid number.");
                    return;
                }
                sys.getRoleManager().removePermissionFromRole(roleName, perms.get(idx));
                System.out.println("Permission removed.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        });

        parser.registerCommand("assign-role", "Assign role to user", (sc, sys) -> {
            if (sys.getCurrentUser() == null) {
                System.out.println("You must be logged in to assign roles.");
                return;
            }

            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            User user = userOpt.get();

            List<Role> roles = sys.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles available.");
                return;
            }

            System.out.println("Available roles:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.printf("%2d) %s%n", i + 1, roles.get(i).getName());
            }

            System.out.print("Choose role number: ");
            try {
                int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
                if (idx < 0 || idx >= roles.size()) {
                    System.out.println("Invalid choice.");
                    return;
                }
                Role role = roles.get(idx);

                System.out.print("Permanent or temporary? (p/t): ");
                String type = sc.nextLine().trim().toLowerCase();

                AssignmentMetadata meta = AssignmentMetadata.now(sys.getCurrentUser(), null);

                RoleAssignment assignment;
                if (type.equals("t")) {
                    System.out.print("Expiration date (yyyy-MM-dd HH:mm): ");
                    String expires = sc.nextLine().trim();
                    System.out.print("Auto-renew? (y/n): ");
                    boolean autoRenew = sc.nextLine().trim().equalsIgnoreCase("y");
                    assignment = new TemporaryAssignment(user, role, meta, expires, autoRenew);
                } else {
                    assignment = new PermanentAssignment(user, role, meta);
                }

                sys.getAssignmentManager().add(assignment);
                System.out.println("Role assigned successfully.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Revoke role from user", (sc, sys) -> {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            Optional<User> opt = sys.getUserManager().findByUsername(username);
            if (opt.isEmpty()) {
                System.out.println("User not found.");
                return;
            }

            List<RoleAssignment> active = sys.getAssignmentManager().findByUser(opt.get())
                    .stream().filter(RoleAssignment::isActive).toList();

            if (active.isEmpty()) {
                System.out.println("No active assignments.");
                return;
            }

            System.out.println("Active assignments:");
            for (int i = 0; i < active.size(); i++) {
                RoleAssignment a = active.get(i);
                System.out.printf("%2d) %s → %s (%s)%n", i + 1, a.role().getName(), a.assignmentType(), a.metadata().assignedAt());
            }

            System.out.print("Choose number to revoke: ");
            try {
                int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
                if (idx < 0 || idx >= active.size()) {
                    System.out.println("Invalid choice.");
                    return;
                }
                sys.getAssignmentManager().revokeAssignment(active.get(idx).assignmentId());
                System.out.println("Assignment revoked.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-list", "List all assignments", (sc, sys) -> {
            List<RoleAssignment> list = sys.getAssignmentManager().findAll();
            printAssignmentsTable(list);
        });

        parser.registerCommand("assignment-list-user", "Assignments for user", (sc, sys) -> {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();
            Optional<User> opt = sys.getUserManager().findByUsername(username);
            if (opt.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            printAssignmentsTable(sys.getAssignmentManager().findByUser(opt.get()));
        });

        parser.registerCommand("assignment-list-role", "Users with role", (sc, sys) -> {
            System.out.print("Enter role name: ");
            String roleName = sc.nextLine().trim();
            Optional<Role> opt = sys.getRoleManager().findByName(roleName);
            if (opt.isEmpty()) {
                System.out.println("Role not found.");
                return;
            }
            List<RoleAssignment> list = sys.getAssignmentManager().findByRole(opt.get());
            if (list.isEmpty()) {
                System.out.println("No assignments found.");
                return;
            }
            System.out.println("Users with role " + roleName + ":");
            list.forEach(a -> System.out.println("  • " + a.user().username() + " (" + (a.isActive() ? "active" : "inactive") + ")"));
        });

        parser.registerCommand("assignment-active", "List active assignments", (sc, sys) -> {
            printAssignmentsTable(sys.getAssignmentManager().getActiveAssignments());
        });

        parser.registerCommand("assignment-expired", "List expired assignments", (sc, sys) -> {
            printAssignmentsTable(sys.getAssignmentManager().getExpiredAssignments());
        });

        parser.registerCommand("assignment-extend", "Extend temporary assignment", (sc, sys) -> {
            System.out.print("Enter assignment ID: ");
            String id = sc.nextLine().trim();

            Optional<RoleAssignment> opt = sys.getAssignmentManager().findById(id);
            if (opt.isEmpty() || !(opt.get() instanceof TemporaryAssignment temp)) {
                System.out.println("Temporary assignment not found.");
                return;
            }

            System.out.print("New expiration (yyyy-MM-dd HH:mm): ");
            String newExp = sc.nextLine().trim();

            try {
                sys.getAssignmentManager().extendTemporaryAssignment(id, newExp);
                System.out.println("Assignment extended.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("permissions-user", "Show user's permissions", (sc, sys) -> {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            Optional<User> opt = sys.getUserManager().findByUsername(username);
            if (opt.isEmpty()) {
                System.out.println("User not found.");
                return;
            }

            Set<Permission> perms = sys.getAssignmentManager().getUserPermissions(opt.get());
            if (perms.isEmpty()) {
                System.out.println("No permissions.");
                return;
            }

            Map<String, List<Permission>> byResource = perms.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

            byResource.forEach((res, list) -> {
                System.out.println("\nResource: " + res);
                list.forEach(p -> System.out.println("  • " + p.name() + " - " + p.description()));
            });
        });

        parser.registerCommand("permissions-check", "Check specific permission", (sc, sys) -> {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            System.out.print("Permission name (e.g. READ): ");
            String permName = sc.nextLine().trim().toUpperCase();

            System.out.print("Resource (e.g. users): ");
            String resource = sc.nextLine().trim().toLowerCase();

            boolean has = sys.getAssignmentManager().userHasPermission(
                    sys.getUserManager().findByUsername(username).orElse(null),
                    permName, resource);

            System.out.println("Has permission: " + (has ? "YES" : "NO"));
        });
        parser.registerCommand("help", "Show available commands", (sc, sys) -> parser.printHelp());

        parser.registerCommand("stats", "Show system statistics", (sc, sys) -> {
            System.out.println(sys.generateStatistics());
        });

        parser.registerCommand("report-users-async", "Generate user report in background", (sc, sys) -> {
            ReportGenerator generator = new ReportGenerator();
            String performer = sys.getCurrentUser() != null ? sys.getCurrentUser() : "system";

            sys.getBackgroundExecutor().submit(() -> {
                String report = generator.generateUserReport(sys.getUserManager(), sys.getAssignmentManager());
                System.out.println(report);
                sys.getAuditLog().log("REPORT_USERS_ASYNC", performer, "users", "User report generated in background");
            });

            System.out.println("User report generation started in background.");
        });

        parser.registerCommand("save-async", "Save audit log to file in background", (sc, sys) -> {
            System.out.print("Enter filename: ");
            String filename = sc.nextLine().trim();
            if (filename.isEmpty()) {
                System.out.println("Filename cannot be empty.");
                return;
            }

            String performer = sys.getCurrentUser() != null ? sys.getCurrentUser() : "system";
            sys.getBackgroundExecutor().submit(() -> {
                sys.getAuditLog().saveToFile(filename);
                sys.getAuditLog().log("SAVE_ASYNC", performer, filename, "Audit log saved in background");
            });

            System.out.println("Save started in background.");
        });

        parser.registerCommand("clear", "Clear screen", (sc, sys) -> {
            for (int i = 0; i < 40; i++) System.out.println();
        });

        parser.registerCommand("exit", "Exit program", (sc, sys) -> {
            System.out.print("Really exit? (y/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("Goodbye.");
                System.exit(0);
            }
        });
    }

    private static void printAssignmentsTable(List<RoleAssignment> assignments) {
        if (assignments.isEmpty()) {
            System.out.println("No assignments found.");
            return;
        }

        System.out.printf("%-15s %-20s %-12s %-8s %-20s%n",
                "Username", "Role", "Type", "Status", "Assigned At");
        System.out.println("-".repeat(75));

        assignments.forEach(a -> {
            String status = a.isActive() ? "ACTIVE" : "INACTIVE";
            System.out.printf("%-15s %-20s %-12s %-8s %-20s%n",
                    a.user().username(),
                    a.role().getName(),
                    a.assignmentType(),
                    status,
                    a.metadata().assignedAt());
        });
    }
}
