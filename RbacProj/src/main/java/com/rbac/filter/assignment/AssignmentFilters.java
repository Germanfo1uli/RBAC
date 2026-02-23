package com.rbac.filter.assignment;

import com.rbac.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentFilters {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter METADATA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AssignmentFilter byUser(User user) {
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return assignment -> assignment.user().username().equalsIgnoreCase(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return assignment -> assignment.role().getName().equalsIgnoreCase(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return assignment -> assignment.assignmentType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return assignment -> assignment.metadata().assignedBy().equalsIgnoreCase(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        return assignment -> {
            LocalDateTime filterDate = LocalDateTime.parse(date, METADATA_FORMATTER);
            LocalDateTime assignedAt = LocalDateTime.parse(assignment.metadata().assignedAt(), METADATA_FORMATTER);
            return assignedAt.isAfter(filterDate);
        };
    }

    public static AssignmentFilter expiringBefore(String date) {
        return assignment -> {
            if (assignment instanceof TemporaryAssignment temp) {
                LocalDateTime filterDate = LocalDateTime.parse(date, DATE_FORMATTER);
                LocalDateTime expiresAt = LocalDateTime.parse(temp.getExpiresAt(), DATE_FORMATTER);
                return expiresAt.isBefore(filterDate);
            }
            return false;
        };
    }
}