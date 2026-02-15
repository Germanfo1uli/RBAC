package org.example;

import com.rbac.model.User;
import com.rbac.model.Permission;

public class Main {
    public static void main(String[] args) {
        System.out.println("1)Testing User Validation\n");
        testUserValidation();

        System.out.println("\n2)Testing Permission\n");
        testPermission();
    }

    private static void testUserValidation() {
        try {
            User validUser = User.validate("jojo", "jozev", "jojo@example.com");
            System.out.println("Valid user created: " + validUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            User.validate("jojo", "jozev", "jojo@example.com");
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

        System.out.println("\n 3)Testing matches method");
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
}