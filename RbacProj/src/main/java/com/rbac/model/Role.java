package com.rbac.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final String id;
    private final String name;
    private String description;
    private final Set<Permission> permissions;


    public Role(String name, String description) {
        this.id = "role_" + UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }


    public Role(String id, String name, String description, Set<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>(permissions);
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission cannot be null");
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission cannot be null");
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission cannot be null");
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                        p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }


    public int getPermissionCount() {
        return permissions.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Role{id='%s', name='%s', description='%s', permissions=%d}",
                id, name, description, permissions.size());
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Role: %s [ID: %s]\n", name, id));
        sb.append(String.format("Description: %s\n", description));
        sb.append(String.format("Permissions (%d):\n", permissions.size()));

        for (Permission permission : permissions) {
            sb.append(String.format(" - %s\n", permission.format()));
        }

        return sb.toString();
    }
}