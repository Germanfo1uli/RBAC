package com.rbac.manager;

import com.rbac.filter.assignment.AssignmentFilter;
import com.rbac.model.*;
import com.rbac.repository.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @Override
    public void add(RoleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");

        synchronized (lock) {
            if (userHasRole(assignment.user(), assignment.role())) {
                throw new IllegalStateException("User already has an active assignment for this role");
            }

            assignments.put(assignment.assignmentId(), assignment);
        }
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) return false;
        synchronized (lock) {
            return assignments.remove(assignment.assignmentId()) != null;
        }
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        synchronized (lock) {
            return Optional.ofNullable(assignments.get(id));
        }
    }

    @Override
    public List<RoleAssignment> findAll() {
        synchronized (lock) {
            return new ArrayList<>(assignments.values());
        }
    }

    @Override
    public int count() {
        synchronized (lock) {
            return assignments.size();
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            assignments.clear();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        synchronized (lock) {
            return assignments.values().stream()
                    .filter(a -> a.user().equals(user))
                    .collect(Collectors.toList());
        }
    }

    public List<RoleAssignment> findByRole(Role role) {
        synchronized (lock) {
            return assignments.values().stream()
                    .filter(a -> a.role().equals(role))
                    .collect(Collectors.toList());
        }
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        synchronized (lock) {
            return assignments.values().stream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        }
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        synchronized (lock) {
            return assignments.values().stream()
                    .filter(filter::test)
                    .sorted(sorter)
                    .collect(Collectors.toList());
        }
    }

    public List<RoleAssignment> getActiveAssignments() {
        synchronized (lock) {
            return assignments.values().stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());
        }
    }

    public List<RoleAssignment> getExpiredAssignments() {
        synchronized (lock) {
            return assignments.values().stream()
                    .filter(a -> !a.isActive())
                    .collect(Collectors.toList());
        }
    }

    public boolean userHasRole(User user, Role role) {
        synchronized (lock) {
            return assignments.values().stream()
                    .anyMatch(a -> a.user().equals(user) &&
                            a.role().equals(role) &&
                            a.isActive());
        }
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        synchronized (lock) {
            return getUserPermissions(user).stream()
                    .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                            p.resource().equalsIgnoreCase(resource));
        }
    }

    public Set<Permission> getUserPermissions(User user) {
        synchronized (lock) {
            return assignments.values().stream()
                    .filter(a -> a.user().equals(user) && a.isActive())
                    .flatMap(a -> a.role().getPermissions().stream())
                    .collect(Collectors.toSet());
        }
    }

    public void revokeAssignment(String assignmentId) {
        synchronized (lock) {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new NoSuchElementException("Assignment not found: " + assignmentId);
            }

            if (assignment instanceof PermanentAssignment permanent) {
                permanent.revoke();
            } else if (assignment instanceof TemporaryAssignment) {
                assignments.remove(assignmentId);
            }
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        synchronized (lock) {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (!(assignment instanceof TemporaryAssignment temporary)) {
                throw new IllegalArgumentException("Assignment is not temporary or not found: " + assignmentId);
            }
            temporary.extend(newExpirationDate);
        }
    }
}
