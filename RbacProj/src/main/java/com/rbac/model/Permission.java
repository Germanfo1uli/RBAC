package com.rbac.model;

import java.util.Objects;

public record Permission(String name, String resource, String description) {

    public Permission {
        Objects.requireNonNull(name, "Permission name cannot be null");
        Objects.requireNonNull(resource, "Resource cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");


        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }
        if (trimmedName.contains(" ")) {
            throw new IllegalArgumentException("Permission name cannot contain spaces");
        }
        if (!trimmedName.equals(trimmedName.toUpperCase())) {
            throw new IllegalArgumentException("Permission name must be in uppercase");
        }

        String trimmedResource = resource.trim();
        if (trimmedResource.isEmpty()) {
            throw new IllegalArgumentException("Resource cannot be empty");
        }
        if (!trimmedResource.equals(trimmedResource.toLowerCase())) {
            throw new IllegalArgumentException("Resource must be in lowercase");
        }

        String trimmedDescription = description.trim();
        if (trimmedDescription.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = namePattern == null ||
                namePattern.isEmpty() ||
                name.contains(namePattern) ||
                name.matches(namePattern);

        boolean resourceMatches = resourcePattern == null ||
                resourcePattern.isEmpty() ||
                resource.contains(resourcePattern) ||
                resource.matches(resourcePattern);

        return nameMatches && resourceMatches;
    }
}