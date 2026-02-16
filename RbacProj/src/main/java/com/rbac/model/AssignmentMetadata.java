package com.rbac.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AssignmentMetadata {
        Objects.requireNonNull(assignedBy, "AssignedBy cannot be null");
        Objects.requireNonNull(assignedAt, "AssignedAt cannot be null");

        if (assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignedBy cannot be empty");
        }

        if (assignedAt.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignedAt cannot be empty");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentDateTime = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, currentDateTime, reason);
    }

    public static AssignmentMetadata now(String assignedBy) {
        return now(assignedBy, null);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Assigned by: %s at %s", assignedBy, assignedAt));

        if (reason != null && !reason.trim().isEmpty()) {
            sb.append(String.format(" | Reason: %s", reason));
        }

        return sb.toString();
    }

    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }
}