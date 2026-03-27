package com.rbac.manager;

import com.rbac.filter.role.RoleFilter;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.repository.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();
    private final Map<String, Role> rolesByName = new ConcurrentHashMap<>();
    private final AssignmentManager assignmentManager;
    private final Object lock = new Object();

    public RoleManager(AssignmentManager assignmentManager) {
        this.assignmentManager = assignmentManager;
    }

    @Override
    public void add(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        synchronized (lock) {
            if (rolesByName.containsKey(role.getName())) {
                throw new IllegalArgumentException("Role with name " + role.getName() + " already exists");
            }
            rolesById.put(role.getId(), role);
            rolesByName.put(role.getName(), role);
        }
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;
        synchronized (lock) {
            if (!assignmentManager.findByRole(role).isEmpty()) {
                throw new IllegalStateException("Cannot remove role: it is currently assigned to users");
            }

            rolesByName.remove(role.getName());
            return rolesById.remove(role.getId()) != null;
        }
    }

    @Override
    public Optional<Role> findById(String id) {
        synchronized (lock) {
            return Optional.ofNullable(rolesById.get(id));
        }
    }

    @Override
    public List<Role> findAll() {
        synchronized (lock) {
            return new ArrayList<>(rolesById.values());
        }
    }

    @Override
    public int count() {
        synchronized (lock) {
            return rolesById.size();
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            rolesById.clear();
            rolesByName.clear();
        }
    }

    public Optional<Role> findByName(String name) {
        synchronized (lock) {
            return Optional.ofNullable(rolesByName.get(name));
        }
    }

    public List<Role> findByFilter(RoleFilter filter) {
        synchronized (lock) {
            return rolesById.values().stream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        }
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        synchronized (lock) {
            return rolesById.values().stream()
                    .filter(filter::test)
                    .sorted(sorter)
                    .collect(Collectors.toList());
        }
    }

    public boolean exists(String name) {
        synchronized (lock) {
            return rolesByName.containsKey(name);
        }
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        synchronized (lock) {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new NoSuchElementException("Role not found: " + roleName);
            }
            role.addPermission(permission);
        }
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        synchronized (lock) {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new NoSuchElementException("Role not found: " + roleName);
            }
            role.removePermission(permission);
        }
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        synchronized (lock) {
            return rolesById.values().stream()
                    .filter(role -> role.hasPermission(permissionName, resource))
                    .collect(Collectors.toList());
        }
    }

}
