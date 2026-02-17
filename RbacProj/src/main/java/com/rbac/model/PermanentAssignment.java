package com.rbac.model;

public class PermanentAssignment extends AbstractRoleAssignment {

    private boolean revoked;

    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
        this.revoked = false;
    }

    public PermanentAssignment(String assignmentId, User user, Role role,
                               AssignmentMetadata metadata, boolean revoked) {
        super(assignmentId, user, role, metadata);
        this.revoked = revoked;
    }

    @Override
    public boolean isActive() {
        return !revoked;
    }

    @Override
    public String assignmentType() {
        return "PERMANENT";
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isRevoked() {
        return revoked;
    }

    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "REVOKED";
        String type = assignmentType();

        return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s",
                type,
                role().getName(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt(),
                metadata().hasReason() ? metadata().reason() : "No reason provided",
                status);
    }
}