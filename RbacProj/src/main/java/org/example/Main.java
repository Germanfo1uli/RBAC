package org.example;

import com.rbac.model.User;
import com.rbac.model.Permission;
import com.rbac.model.Role;

public class Main {
    public static void main(String[] args) {
        System.out.println("1)Testing User Validation\n");
        testUserValidation();

        System.out.println("\n2)Testing Permission\n");
        testPermission();

        System.out.println("\n3)Testing Role\n");
        testRole();
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
}