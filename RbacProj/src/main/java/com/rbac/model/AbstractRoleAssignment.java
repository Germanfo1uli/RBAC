package com.rbac.model;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {

    protected final String assignmentId;
    protected final User user;
    protected final Role role;
    protected final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = "assign_" + UUID.randomUUID().toString();
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }

    public AbstractRoleAssignment(String assignmentId, User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = Objects.requireNonNull(assignmentId, "AssignmentId cannot be null");
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    public String summary() {
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        String type = assignmentType();

        return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s",
                type,
                role.getName(),
                user.username(),
                metadata.assignedBy(),
                metadata.assignedAt(),
                metadata.hasReason() ? metadata.reason() : "No reason provided",
                status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', user='%s', role='%s', active=%s}",
                getClass().getSimpleName(),
                assignmentId,
                user.username(),
                role.getName(),
                isActive());
    }
}