package org.example;

import com.rbac.model.User;

public class Main {
    public static void main(String[] args) {
        System.out.println("Just Testing User Validation\n");

        try {
            User validUser = User.validate("jozev", "jostar", "jojo@example.com");
            System.out.println("Valid user created: " + validUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            User invalidUser = User.validate("jo", "jostar", "jojo@example.com");
            System.out.println("Valid user created: " + invalidUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error with short username: " + e.getMessage());
        }

        try {
            User invalidUser = User.validate("jozev@jostar", "jostar", "jojo@example.com");
            System.out.println("Valid user created: " + invalidUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error with special chars: " + e.getMessage());
        }

        try {
            User invalidUser = User.validate("jo_jo", "jozev", "jojo.example.com");
            System.out.println("Valid user created: " + invalidUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error with invalid email: " + e.getMessage());
        }


        try {
            User invalidUser = User.validate("", "John Doe", "john@example.com");
            System.out.println("Valid user created: " + invalidUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error with empty username: " + e.getMessage());
        }
    }
}