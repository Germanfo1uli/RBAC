package com.rbac.filter.assignment;

import com.rbac.model.RoleAssignment;
import java.util.Objects;

@FunctionalInterface
public interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    default AssignmentFilter and(AssignmentFilter other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default AssignmentFilter or(AssignmentFilter other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    default AssignmentFilter negate() {
        return (t) -> !test(t);
    }
}