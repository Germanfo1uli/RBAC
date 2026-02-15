package com.rbac.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public User {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(fullName, "Full name cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");

        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must contain only Latin letters, digits, and underscore, " +
                            "and be between 3 and 20 characters long"
            );
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Email must be in valid format (contain @ and a dot after @)"
            );
        }
    }

    public static User validate(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}