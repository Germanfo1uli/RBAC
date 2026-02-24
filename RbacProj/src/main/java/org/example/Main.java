package org.example;

import com.rbac.model.User;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.model.AssignmentMetadata;
import com.rbac.model.PermanentAssignment;
import com.rbac.model.TemporaryAssignment;
import com.rbac.model.RoleAssignment;
import com.rbac.manager.UserManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.AssignmentManager;
import com.rbac.filter.user.UserFilter;
import com.rbac.filter.user.UserFilters;
import com.rbac.filter.role.RoleFilter;
import com.rbac.filter.role.RoleFilters;
import com.rbac.filter.assignment.AssignmentFilter;
import com.rbac.filter.assignment.AssignmentFilters;
import com.rbac.sorter.UserSorters;
import com.rbac.sorter.RoleSorters;
import com.rbac.sorter.AssignmentSorters;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


public class Main {
    public static void main(String[] args) {
        System.out.println("1) Testing User Validation\n");
        testUserValidation();

        System.out.println("\n2) Testing Permission\n");
        testPermission();

        System.out.println("\n3) Testing Role\n");
        testRole();

        System.out.println("\n4) Testing AssignmentMetadata\n");
        testAssignmentMetadata();

        System.out.println("\n5) Testing PermanentAssignment\n");
        testPermanentAssignment();

        System.out.println("\n6) Testing TemporaryAssignment\n");
        testTemporaryAssignment();

        System.out.println("\n7) Testing UserFilters and UserSorters\n");
        testUserFilters();

        System.out.println("\n8) Testing RoleFilters and RoleSorters\n");
        testRoleFilters();

        System.out.println("\n9) Testing AssignmentFilters and AssignmentSorters\n");
        testAssignmentFilters();

        System.out.println("\n10) Testing UserManager\n");
        testUserManager();

        System.out.println("\n11) Testing RoleManager\n");
        testRoleManager();

        System.out.println("\n12) Testing AssignmentManager\n");
        testAssignmentManager();

        System.out.println("\n13) Testing Integration\n");
        testIntegration();
    }

    private static void testUserValidation() {
        try {
            User validUser = User.validate("jojo", "jozev", "jojo@example.com");
            System.out.println("Valid user created: " + validUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            User.validate("jo", "jozev", "jojo@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught short username: " + e.getMessage());
        }

        try {
            User.validate("jojo@jozev", "jozev", "jojo@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught special chars: " + e.getMessage());
        }

        try {
            User.validate("jojo", "John Doe", "john.example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught invalid email: " + e.getMessage());
        }
    }

    private static void testPermission() {
        try {
            Permission readUsers = new Permission("READ", "users", "Can read user data");
            System.out.println("Permission created: " + readUsers.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            Permission writeReports = new Permission("write", "reports", "Can modify reports");
            System.out.println("Should have failed: " + writeReports.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught lowercase name: " + e.getMessage());
        }

        try {
            Permission writeReports = new Permission("WRITE", "REPORTS", "Can modify reports");
            System.out.println("Should have failed: " + writeReports.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught uppercase resource: " + e.getMessage());
        }

        try {
            Permission invalid = new Permission("READ DATA", "users", "Description");
            System.out.println("Should have failed: " + invalid.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught space in name: " + e.getMessage());
        }

        System.out.println("\nTesting matches method");
        try {
            Permission deleteSettings = new Permission("DELETE", "settings", "Can delete settings");
            System.out.println("DELETE matches 'DEL': " + deleteSettings.matches("DEL", null));
            System.out.println("DELETE matches 'WRITE': " + deleteSettings.matches("WRITE", null));
            System.out.println("settings matches 'set': " + deleteSettings.matches(null, "set"));
            System.out.println("Both patterns: " + deleteSettings.matches("DEL", "set"));
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating permission: " + e.getMessage());
        }
    }

    private static void testRole() {
        try {
            Permission readUsers = new Permission("READ", "users", "Can view user list");
            Permission writeUsers = new Permission("WRITE", "users", "Can create and edit users");
            Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
            Permission readReports = new Permission("READ", "reports", "Can view reports");

            System.out.println("Creating Administrator role:");
            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(readUsers);
            adminRole.addPermission(writeUsers);
            adminRole.addPermission(deleteUsers);
            adminRole.addPermission(readReports);
            System.out.println(adminRole.format());

            System.out.println("\nCreating Viewer role:");
            Role viewerRole = new Role("Viewer", "Read-only access");
            viewerRole.addPermission(readUsers);
            viewerRole.addPermission(readReports);
            System.out.println(viewerRole.format());

            System.out.println("\nTesting role methods:");
            System.out.println("Admin has READ on users? " + adminRole.hasPermission("READ", "users"));
            System.out.println("Admin has WRITE on reports? " + adminRole.hasPermission("WRITE", "reports"));
            System.out.println("Viewer has DELETE on users? " + viewerRole.hasPermission(deleteUsers));

            System.out.println("\nTesting remove permission:");
            System.out.println("Admin permissions before: " + adminRole.getPermissionCount());
            adminRole.removePermission(readReports);
            System.out.println("Admin permissions after removing READ on reports: " + adminRole.getPermissionCount());

            System.out.println("\nTesting unmodifiable set:");
            try {
                adminRole.getPermissions().add(readReports);
                System.out.println("Should not be able to modify");
            } catch (UnsupportedOperationException e) {
                System.out.println("Cannot modify returned set (correct)");
            }

            System.out.println("\nTesting equals and hashCode:");
            Role anotherAdmin = new Role(adminRole.getId(), "Administrator", "Another admin", adminRole.getPermissions());
            System.out.println("Same ID? " + adminRole.equals(anotherAdmin));
            System.out.println("Same hash? " + (adminRole.hashCode() == anotherAdmin.hashCode()));

            Role differentRole = new Role("Manager", "Manager role");
            System.out.println("Different ID? " + adminRole.equals(differentRole));

            System.out.println("\nTesting toString:");
            System.out.println(adminRole);

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testAssignmentMetadata() {
        try {
            System.out.println("Creating metadata with reason:");
            AssignmentMetadata metadata1 = AssignmentMetadata.now("jojo", "Initial role assignment");
            System.out.println(metadata1.format());

            System.out.println("\nCreating metadata without reason:");
            AssignmentMetadata metadata2 = AssignmentMetadata.now("jozev");
            System.out.println(metadata2.format());

            System.out.println("\nCreating metadata with specific date:");
            AssignmentMetadata metadata3 = new AssignmentMetadata("jojo", "2026-02-16 10:30:00", "Project access");
            System.out.println(metadata3.format());

            System.out.println("\nTesting hasReason method:");
            System.out.println("Metadata1 has reason? " + metadata1.hasReason());
            System.out.println("Metadata2 has reason? " + metadata2.hasReason());

            System.out.println("\nTesting validation - empty assignedBy:");
            try {
                AssignmentMetadata invalid = new AssignmentMetadata("", "2026-02-16 10:30:00", "Reason");
                System.out.println("Should have failed: " + invalid.format());
            } catch (IllegalArgumentException e) {
                System.out.println("Caught empty assignedBy: " + e.getMessage());
            }

            System.out.println("\nTesting validation - null assignedAt:");
            try {
                AssignmentMetadata invalid = new AssignmentMetadata("jojo", null, "Reason");
                System.out.println("Should have failed: " + invalid.format());
            } catch (NullPointerException e) {
                System.out.println("Caught null assignedAt: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testPermanentAssignment() {
        try {
            User user = User.validate("jojo", "jozev", "jojo@example.com");

            Permission readUsers = new Permission("READ", "users", "Can view user list");
            Permission writeUsers = new Permission("WRITE", "users", "Can create and edit users");

            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(readUsers);
            adminRole.addPermission(writeUsers);

            AssignmentMetadata metadata = AssignmentMetadata.now("jozev", "Permanent admin access");

            System.out.println("1. Creating PermanentAssignment:");
            PermanentAssignment assignment = new PermanentAssignment(user, adminRole, metadata);
            System.out.println("Assignment ID: " + assignment.assignmentId());
            System.out.println("Type: " + assignment.assignmentType());
            System.out.println("Active: " + assignment.isActive());
            System.out.println("Revoked: " + assignment.isRevoked());

            System.out.println("\n2. Testing summary():");
            System.out.println(assignment.summary());

            System.out.println("\n3. Testing revoke():");
            assignment.revoke();
            System.out.println("After revoke - Active: " + assignment.isActive());
            System.out.println("After revoke - Revoked: " + assignment.isRevoked());

            System.out.println("\n4. Testing summary after revoke:");
            System.out.println(assignment.summary());

            System.out.println("\n5. Testing constructor with existing ID:");
            PermanentAssignment existingAssignment = new PermanentAssignment(
                    assignment.assignmentId(), user, adminRole, metadata, true);
            System.out.println("Same ID? " + assignment.equals(existingAssignment));
            System.out.println("Active: " + existingAssignment.isActive());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testTemporaryAssignment() {
        try {
            User user = User.validate("jojo", "jozev", "jojo@example.com");

            Permission readUsers = new Permission("READ", "users", "Can view user list");

            Role viewerRole = new Role("Viewer", "Read-only access");
            viewerRole.addPermission(readUsers);

            AssignmentMetadata metadata = AssignmentMetadata.now("jozev", "Temporary project access");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureDate = now.plusDays(7);
            String expiresAt = futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            System.out.println("1. Creating TemporaryAssignment (expires in 7 days):");
            TemporaryAssignment assignment = new TemporaryAssignment(user, viewerRole, metadata, expiresAt, true);
            System.out.println("Assignment ID: " + assignment.assignmentId());
            System.out.println("Type: " + assignment.assignmentType());
            System.out.println("Expires: " + assignment.getExpiresAt());
            System.out.println("Auto-renew: " + assignment.isAutoRenew());

            System.out.println("\n2. Testing isActive() with current time:");
            System.out.println("Active now: " + assignment.isActive(now));

            System.out.println("\n3. Testing getTimeRemaining():");
            System.out.println("Time remaining: " + assignment.getTimeRemaining(now));

            System.out.println("\n4. Testing summary():");
            System.out.println(assignment.summary());

            System.out.println("\n5. Testing isExpired() with future date:");
            LocalDateTime expiredDate = futureDate.plusDays(1);
            System.out.println("Active on " + expiredDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    ": " + assignment.isActive(expiredDate));

            System.out.println("\n6. Testing extend():");
            LocalDateTime extendedDate = now.plusDays(14);
            String newExpiresAt = extendedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            assignment.extend(newExpiresAt);
            System.out.println("New expiration: " + assignment.getExpiresAt());
            System.out.println("Time remaining now: " + assignment.getTimeRemaining(now));

            System.out.println("\n7. Testing auto-renew toggle:");
            assignment.setAutoRenew(false);
            System.out.println("Auto-renew now: " + assignment.isAutoRenew());

            System.out.println("\n8. Testing expired assignment:");
            LocalDateTime pastDate = now.minusDays(1);
            String expiredExpiresAt = pastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            TemporaryAssignment expiredAssignment = new TemporaryAssignment(
                    user, viewerRole, metadata, expiredExpiresAt, false);
            System.out.println("Is expired: " + expiredAssignment.isExpired(now));
            System.out.println("Is active: " + expiredAssignment.isActive(now));
            System.out.println("Time remaining: " + expiredAssignment.getTimeRemaining(now));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testUserFilters() {
        try {
            User user1 = User.validate("john", "John Doe", "john@company.com");
            User user2 = User.validate("jane", "Jane Smith", "jane@gmail.com");
            User user3 = User.validate("bob", "Bob Johnson", "bob@company.com");

            List<User> users = List.of(user1, user2, user3);

            System.out.println("Testing byUsername filter:");
            UserFilter filter1 = UserFilters.byUsername("john");
            users.stream().filter(filter1::test).forEach(u ->
                    System.out.println("Found: " + u.username()));

            System.out.println("\nTesting byUsernameContains filter:");
            UserFilter filter2 = UserFilters.byUsernameContains("JO");
            users.stream().filter(filter2::test).forEach(u ->
                    System.out.println("Found: " + u.username()));

            System.out.println("\nTesting byEmailDomain filter:");
            UserFilter filter3 = UserFilters.byEmailDomain("@company.com");
            users.stream().filter(filter3::test).forEach(u ->
                    System.out.println("Found: " + u.email()));

            System.out.println("\nTesting combined filters (and):");
            UserFilter filter4 = UserFilters.byEmailDomain("@company.com")
                    .and(UserFilters.byUsernameContains("j"));
            users.stream().filter(filter4::test).forEach(u ->
                    System.out.println("Found: " + u.username() + " - " + u.email()));

            System.out.println("\nTesting sorters:");
            System.out.println("Sorted by username:");
            users.stream().sorted(UserSorters.byUsername()).forEach(u ->
                    System.out.println("  " + u.username()));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testRoleFilters() {
        try {
            Permission p1 = new Permission("READ", "users", "Read users");
            Permission p2 = new Permission("WRITE", "users", "Write users");
            Permission p3 = new Permission("READ", "reports", "Read reports");

            Role role1 = new Role("Admin", "Administrator");
            role1.addPermission(p1);
            role1.addPermission(p2);
            role1.addPermission(p3);

            Role role2 = new Role("Viewer", "Viewer role");
            role2.addPermission(p1);
            role2.addPermission(p3);

            Role role3 = new Role("Editor", "Editor role");
            role3.addPermission(p2);

            List<Role> roles = List.of(role1, role2, role3);

            System.out.println("Testing byName filter:");
            RoleFilter filter1 = RoleFilters.byName("Admin");
            roles.stream().filter(filter1::test).forEach(r ->
                    System.out.println("Found: " + r.getName()));

            System.out.println("\nTesting hasPermission filter:");
            RoleFilter filter2 = RoleFilters.hasPermission(p2);
            roles.stream().filter(filter2::test).forEach(r ->
                    System.out.println("Has WRITE on users: " + r.getName()));

            System.out.println("\nTesting hasAtLeastNPermissions filter:");
            RoleFilter filter3 = RoleFilters.hasAtLeastNPermissions(2);
            roles.stream().filter(filter3::test).forEach(r ->
                    System.out.println(r.getName() + " has " + r.getPermissionCount() + " permissions"));

            System.out.println("\nTesting sorters by permission count:");
            roles.stream().sorted(RoleSorters.byPermissionCount()).forEach(r ->
                    System.out.println(r.getName() + ": " + r.getPermissionCount()));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testAssignmentFilters() {
        try {
            User user1 = User.validate("john", "John Doe", "john@company.com");
            User user2 = User.validate("jane", "Jane Smith", "jane@company.com");

            Permission p1 = new Permission("READ", "users", "Read users");
            Permission p2 = new Permission("WRITE", "users", "Write users");

            Role role1 = new Role("Admin", "Administrator");
            role1.addPermission(p1);
            role1.addPermission(p2);

            Role role2 = new Role("Viewer", "Viewer");
            role2.addPermission(p1);

            AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Assignment 1");
            AssignmentMetadata meta2 = AssignmentMetadata.now("manager", "Assignment 2");
            AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Assignment 3");

            PermanentAssignment perm1 = new PermanentAssignment(user1, role1, meta1);
            TemporaryAssignment temp1 = new TemporaryAssignment(user1, role2, meta2,
                    LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), false);
            PermanentAssignment perm2 = new PermanentAssignment(user2, role2, meta3);

            List<RoleAssignment> assignments = List.of(perm1, temp1, perm2);

            System.out.println("Testing byUser filter:");
            AssignmentFilter filter1 = AssignmentFilters.byUser(user1);
            assignments.stream().filter(filter1::test).forEach(a ->
                    System.out.println("User " + a.user().username() + " has role " + a.role().getName()));

            System.out.println("\nTesting activeOnly filter:");
            AssignmentFilter filter2 = AssignmentFilters.activeOnly();
            assignments.stream().filter(filter2::test).forEach(a ->
                    System.out.println("Active: " + a.role().getName() + " for " + a.user().username()));

            System.out.println("\nTesting byType filter:");
            AssignmentFilter filter3 = AssignmentFilters.byType("TEMPORARY");
            assignments.stream().filter(filter3::test).forEach(a ->
                    System.out.println("Temporary: " + a.role().getName()));

            System.out.println("\nTesting assignedBy filter:");
            AssignmentFilter filter4 = AssignmentFilters.assignedBy("admin");
            assignments.stream().filter(filter4::test).forEach(a ->
                    System.out.println("Assigned by admin: " + a.role().getName() + " to " + a.user().username()));

            System.out.println("\nTesting sorters by assignment date:");
            assignments.stream().sorted(AssignmentSorters.byAssignmentDate()).forEach(a ->
                    System.out.println(a.metadata().assignedAt() + " - " + a.role().getName()));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testUserManager() {
        try {
            UserManager userManager = new UserManager();

            System.out.println("Adding users:");
            userManager.add(User.validate("john", "John Doe", "john@example.com"));
            userManager.add(User.validate("jane", "Jane Smith", "jane@example.com"));
            userManager.add(User.validate("bob", "Bob Wilson", "bob@company.com"));

            System.out.println("Total users: " + userManager.count());

            System.out.println("\nFind by username:");
            Optional<User> found = userManager.findByUsername("john");
            found.ifPresent(u -> System.out.println("Found: " + u.format()));

            System.out.println("\nFind by email:");
            found = userManager.findByEmail("jane@example.com");
            found.ifPresent(u -> System.out.println("Found: " + u.format()));

            System.out.println("\nFind by filter (company domain):");
            List<User> companyUsers = userManager.findByFilter(
                    UserFilters.byEmailDomain("@company.com"));
            companyUsers.forEach(u -> System.out.println(u.format()));

            System.out.println("\nUpdate user:");
            userManager.update("john", "John Updated", "john.new@example.com");
            userManager.findByUsername("john").ifPresent(u ->
                    System.out.println("Updated: " + u.format()));

            System.out.println("\nExists check:");
            System.out.println("User 'john' exists: " + userManager.exists("john"));
            System.out.println("User 'unknown' exists: " + userManager.exists("unknown"));

            System.out.println("\nRemove user:");
            User user = userManager.findByUsername("bob").get();
            userManager.remove(user);
            System.out.println("After removal, total: " + userManager.count());

            System.out.println("\nFind with filter and sort:");
            List<User> sortedUsers = userManager.findAll(
                    UserFilters.byUsernameContains("j"),
                    UserSorters.byEmail());
            sortedUsers.forEach(u -> System.out.println(u.format()));

            userManager.clear();
            System.out.println("\nAfter clear, count: " + userManager.count());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testRoleManager() {
        try {
            AssignmentManager assignmentManager = new AssignmentManager();
            RoleManager roleManager = new RoleManager(assignmentManager);

            Permission readUsers = new Permission("READ", "users", "Read users");
            Permission writeUsers = new Permission("WRITE", "users", "Write users");
            Permission readReports = new Permission("READ", "reports", "Read reports");

            System.out.println("Adding roles:");
            Role adminRole = new Role("Admin", "Administrator role");
            adminRole.addPermission(readUsers);
            adminRole.addPermission(writeUsers);

            Role viewerRole = new Role("Viewer", "Viewer role");
            viewerRole.addPermission(readUsers);
            viewerRole.addPermission(readReports);

            Role editorRole = new Role("Editor", "Editor role");
            editorRole.addPermission(writeUsers);

            roleManager.add(adminRole);
            roleManager.add(viewerRole);
            roleManager.add(editorRole);

            System.out.println("Total roles: " + roleManager.count());

            System.out.println("\nFind by name:");
            Optional<Role> found = roleManager.findByName("Admin");
            found.ifPresent(r -> System.out.println("Found: " + r.getName()));

            System.out.println("\nFind by filter (has at least 2 permissions):");
            List<Role> rolesWithManyPerms = roleManager.findByFilter(
                    RoleFilters.hasAtLeastNPermissions(2));
            rolesWithManyPerms.forEach(r ->
                    System.out.println(r.getName() + " has " + r.getPermissionCount() + " permissions"));

            System.out.println("\nAdd permission to role:");
            Permission deleteUsers = new Permission("DELETE", "users", "Delete users");
            roleManager.addPermissionToRole("Admin", deleteUsers);
            roleManager.findByName("Admin").ifPresent(r ->
                    System.out.println("Admin now has " + r.getPermissionCount() + " permissions"));

            System.out.println("\nRemove permission from role:");
            roleManager.removePermissionFromRole("Admin", readUsers);
            roleManager.findByName("Admin").ifPresent(r ->
                    System.out.println("Admin now has " + r.getPermissionCount() + " permissions"));

            System.out.println("\nFind roles with permission:");
            List<Role> rolesWithReadUsers = roleManager.findRolesWithPermission("READ", "users");
            rolesWithReadUsers.forEach(r ->
                    System.out.println(r.getName() + " has READ on users"));

            System.out.println("\nExists check:");
            System.out.println("Role 'Admin' exists: " + roleManager.exists("Admin"));
            System.out.println("Role 'SuperAdmin' exists: " + roleManager.exists("SuperAdmin"));

            System.out.println("\nFind with filter and sort by permission count:");
            List<Role> sortedRoles = roleManager.findAll(
                    RoleFilters.hasAtLeastNPermissions(1),
                    RoleSorters.byPermissionCount().reversed());
            sortedRoles.forEach(r ->
                    System.out.println(r.getName() + ": " + r.getPermissionCount()));

            roleManager.clear();
            System.out.println("\nAfter clear, count: " + roleManager.count());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testAssignmentManager() {
        try {
            AssignmentManager assignmentManager = new AssignmentManager();

            User user1 = User.validate("john", "John Doe", "john@example.com");
            User user2 = User.validate("jane", "Jane Smith", "jane@example.com");

            Permission readUsers = new Permission("READ", "users", "Read users");
            Permission writeUsers = new Permission("WRITE", "users", "Write users");

            Role adminRole = new Role("Admin", "Administrator");
            adminRole.addPermission(readUsers);
            adminRole.addPermission(writeUsers);

            Role viewerRole = new Role("Viewer", "Viewer");
            viewerRole.addPermission(readUsers);

            AssignmentMetadata meta1 = AssignmentMetadata.now("manager", "Permanent admin");
            AssignmentMetadata meta2 = AssignmentMetadata.now("manager", "Temporary viewer");
            AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Permanent viewer");

            PermanentAssignment perm1 = new PermanentAssignment(user1, adminRole, meta1);

            String expiresAt = LocalDateTime.now().plusDays(30)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            TemporaryAssignment temp1 = new TemporaryAssignment(user1, viewerRole, meta2, expiresAt, true);

            PermanentAssignment perm2 = new PermanentAssignment(user2, viewerRole, meta3);

            System.out.println("Adding assignments:");
            assignmentManager.add(perm1);
            assignmentManager.add(temp1);
            assignmentManager.add(perm2);

            System.out.println("Total assignments: " + assignmentManager.count());

            System.out.println("\nFind by user:");
            List<RoleAssignment> userAssignments = assignmentManager.findByUser(user1);
            userAssignments.forEach(a ->
                    System.out.println(a.user().username() + " - " + a.role().getName() +
                            " (" + a.assignmentType() + ")"));

            System.out.println("\nFind by role:");
            List<RoleAssignment> roleAssignments = assignmentManager.findByRole(viewerRole);
            roleAssignments.forEach(a ->
                    System.out.println("Viewer role assigned to: " + a.user().username()));

            System.out.println("\nGet active assignments:");
            List<RoleAssignment> active = assignmentManager.getActiveAssignments();
            active.forEach(a ->
                    System.out.println("Active: " + a.user().username() + " - " + a.role().getName()));

            System.out.println("\nCheck user has role:");
            boolean hasRole = assignmentManager.userHasRole(user1, adminRole);
            System.out.println("User1 has Admin role: " + hasRole);

            System.out.println("\nGet user permissions:");
            var permissions = assignmentManager.getUserPermissions(user1);
            permissions.forEach(p ->
                    System.out.println("Permission: " + p.format()));

            System.out.println("\nCheck user has permission:");
            boolean hasPermission = assignmentManager.userHasPermission(user1, "READ", "users");
            System.out.println("User1 has READ on users: " + hasPermission);

            System.out.println("\nFind by filter (active assignments for user1):");
            AssignmentFilter filter = AssignmentFilters.byUser(user1)
                    .and(AssignmentFilters.activeOnly());
            List<RoleAssignment> filtered = assignmentManager.findByFilter(filter);
            filtered.forEach(a ->
                    System.out.println("Filtered: " + a.user().username() + " - " + a.role().getName()));

            System.out.println("\nTest revoke permanent assignment:");
            assignmentManager.revokeAssignment(perm1.assignmentId());
            System.out.println("After revoke, active assignments for user1: " +
                    assignmentManager.findByUser(user1).stream().filter(RoleAssignment::isActive).count());

            System.out.println("\nTest extend temporary assignment:");
            String newExpiry = LocalDateTime.now().plusDays(60)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            assignmentManager.extendTemporaryAssignment(temp1.assignmentId(), newExpiry);
            System.out.println("Temporary assignment extended");

            System.out.println("\nFind with filter and sort:");
            List<RoleAssignment> sorted = assignmentManager.findAll(
                    AssignmentFilters.activeOnly(),
                    AssignmentSorters.byUsername());
            sorted.forEach(a ->
                    System.out.println("Sorted by username: " + a.user().username() + " - " + a.role().getName()));

            assignmentManager.clear();
            System.out.println("\nAfter clear, count: " + assignmentManager.count());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testIntegration() {
        try {
            System.out.println("=== INTEGRATION TEST ===");

            UserManager userManager = new UserManager();
            AssignmentManager assignmentManager = new AssignmentManager();
            RoleManager roleManager = new RoleManager(assignmentManager);

            System.out.println("1. Creating users:");
            User alice = User.validate("alice", "Alice Wonder", "alice@company.com");
            User bob = User.validate("bob", "Bob Builder", "bob@company.com");
            User charlie = User.validate("charlie", "Charlie Brown", "charlie@gmail.com");

            userManager.add(alice);
            userManager.add(bob);
            userManager.add(charlie);
            System.out.println("Users created: " + userManager.count());

            System.out.println("\n2. Creating permissions:");
            Permission readUsers = new Permission("READ", "users", "View users");
            Permission writeUsers = new Permission("WRITE", "users", "Modify users");
            Permission deleteUsers = new Permission("DELETE", "users", "Remove users");
            Permission readReports = new Permission("READ", "reports", "View reports");
            Permission writeReports = new Permission("WRITE", "reports", "Modify reports");

            System.out.println("\n3. Creating roles:");
            Role adminRole = new Role("ADMIN", "Full access");
            adminRole.addPermission(readUsers);
            adminRole.addPermission(writeUsers);
            adminRole.addPermission(deleteUsers);
            adminRole.addPermission(readReports);
            adminRole.addPermission(writeReports);

            Role managerRole = new Role("MANAGER", "Can manage users and view reports");
            managerRole.addPermission(readUsers);
            managerRole.addPermission(writeUsers);
            managerRole.addPermission(readReports);

            Role viewerRole = new Role("VIEWER", "Read-only access");
            viewerRole.addPermission(readUsers);
            viewerRole.addPermission(readReports);

            roleManager.add(adminRole);
            roleManager.add(managerRole);
            roleManager.add(viewerRole);
            System.out.println("Roles created: " + roleManager.count());

            System.out.println("\n4. Creating assignments:");
            AssignmentMetadata meta1 = AssignmentMetadata.now("system", "Alice is admin");
            AssignmentMetadata meta2 = AssignmentMetadata.now("system", "Bob is manager");

            String expiresAt = LocalDateTime.now().plusMonths(3)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            AssignmentMetadata meta3 = AssignmentMetadata.now("system", "Charlie temporary viewer");

            PermanentAssignment assign1 = new PermanentAssignment(alice, adminRole, meta1);
            PermanentAssignment assign2 = new PermanentAssignment(bob, managerRole, meta2);
            TemporaryAssignment assign3 = new TemporaryAssignment(charlie, viewerRole, meta3, expiresAt, false);

            assignmentManager.add(assign1);
            assignmentManager.add(assign2);
            assignmentManager.add(assign3);
            System.out.println("Assignments created: " + assignmentManager.count());

            System.out.println("\n5. Testing permissions:");
            System.out.println("Alice has DELETE on users: " +
                    assignmentManager.userHasPermission(alice, "DELETE", "users"));
            System.out.println("Bob has DELETE on users: " +
                    assignmentManager.userHasPermission(bob, "DELETE", "users"));
            System.out.println("Charlie has WRITE on reports: " +
                    assignmentManager.userHasPermission(charlie, "WRITE", "reports"));

            System.out.println("\n6. Alice's permissions:");
            assignmentManager.getUserPermissions(alice).forEach(p ->
                    System.out.println("  " + p.format()));

            System.out.println("\n7. Find company users (by email domain):");
            List<User> companyUsers = userManager.findByFilter(
                    UserFilters.byEmailDomain("@company.com"));
            companyUsers.forEach(u -> System.out.println("  " + u.format()));

            System.out.println("\n8. Find roles with at least 3 permissions:");
            List<Role> powerfulRoles = roleManager.findByFilter(
                    RoleFilters.hasAtLeastNPermissions(3));
            powerfulRoles.forEach(r ->
                    System.out.println("  " + r.getName() + ": " + r.getPermissionCount() + " permissions"));

            System.out.println("\n9. Find active assignments sorted by username:");
            List<RoleAssignment> activeAssignments = assignmentManager.findAll(
                    AssignmentFilters.activeOnly(),
                    AssignmentSorters.byUsername());
            activeAssignments.forEach(a ->
                    System.out.println("  " + a.user().username() + " -> " + a.role().getName()));

            System.out.println("\n10. Revoke Charlie's assignment:");
            assignmentManager.revokeAssignment(assign3.assignmentId());
            System.out.println("Charlie active after revoke: " +
                    assignmentManager.userHasRole(charlie, viewerRole));

            System.out.println("\n=== INTEGRATION TEST COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.out.println("Integration test error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}