package com.rbac.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {

    static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        setExpiresAt(expiresAt);
        this.autoRenew = autoRenew;
    }

    public TemporaryAssignment(String assignmentId, User user, Role role,
                               AssignmentMetadata metadata, String expiresAt,
                               boolean autoRenew) {
        super(assignmentId, user, role, metadata);
        setExpiresAt(expiresAt);
        this.autoRenew = autoRenew;
    }

    private void setExpiresAt(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");
        }
        try {
            LocalDateTime.parse(expiresAt.trim(), FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("ExpiresAt must be in format: yyyy-MM-dd HH:mm");
        }
        this.expiresAt = expiresAt.trim();
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    public boolean isActive(LocalDateTime currentDateTime) {
        return !isExpired(currentDateTime);
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        setExpiresAt(newExpirationDate);
    }

    public boolean isExpired() {
        return isExpired(LocalDateTime.now());
    }

    public boolean isExpired(LocalDateTime currentDateTime) {
        LocalDateTime expiry = LocalDateTime.parse(expiresAt, FORMATTER);
        return currentDateTime.isAfter(expiry);
    }

    public String getTimeRemaining() {
        return getTimeRemaining(LocalDateTime.now());
    }

    public String getTimeRemaining(LocalDateTime currentDateTime) {
        if (isExpired(currentDateTime)) {
            return "Expired";
        }

        LocalDateTime expiry = LocalDateTime.parse(expiresAt, FORMATTER);

        long days = ChronoUnit.DAYS.between(currentDateTime, expiry);
        long hours = ChronoUnit.HOURS.between(currentDateTime, expiry) % 24;
        long minutes = ChronoUnit.MINUTES.between(currentDateTime, expiry) % 60;

        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else {
            return String.format("%d minutes", minutes);
        }
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "EXPIRED";
        String renewStatus = autoRenew ? " (auto-renew)" : "";
        String type = assignmentType();

        return String.format("[%s%s] %s assigned to %s by %s at %s\nExpires: %s\nReason: %s\nStatus: %s",
                type,
                renewStatus,
                role().getName(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt(),
                expiresAt,
                metadata().hasReason() ? metadata().reason() : "No reason provided",
                status);
    }
}