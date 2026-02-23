package com.rbac.filter;

import com.rbac.model.Permission;
import com.rbac.model.Role;

public class RoleFilters {

    public static RoleFilter byName(String name) {
        return (role) -> role.getName().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        return (role) -> role.getName().toLowerCase().contains(substring.toLowerCase());
    }

    public static RoleFilter hasPermission(Permission permission) {
        return (role) -> role.getPermissions().contains(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        return (role) -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return (role) -> role.getPermissionCount() >= n;
    }
}