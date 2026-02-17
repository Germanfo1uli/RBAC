package org.example;

import com.rbac.model.User;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.model.AssignmentMetadata;
import com.rbac.model.PermanentAssignment;
import com.rbac.model.TemporaryAssignment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        System.out.println("1)Testing User Validation\n");
        testUserValidation();

        System.out.println("\n2)Testing Permission\n");
        testPermission();

        System.out.println("\n3)Testing Role\n");
        testRole();

        System.out.println("\n4)Testing AssignmentMetadata\n");
        testAssignmentMetadata();

        System.out.println("\n5)Testing PermanentAssignment\n");
        testPermanentAssignment();

        System.out.println("\n6)Testing TemporaryAssignment\n");
        testTemporaryAssignment();
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
}