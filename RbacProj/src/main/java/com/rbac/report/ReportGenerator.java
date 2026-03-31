package com.rbac.report;

import com.rbac.manager.AssignmentManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.UserManager;
import com.rbac.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USER ROLE REPORT ===\n");
        sb.append(String.format("%-15s | %-25s | %s\n", "Username", "Full Name", "Active Roles"));
        sb.append("-".repeat(70)).append("\n");

        String userLines = userManager.findAll().parallelStream()
                .map(user -> {
                    List<RoleAssignment> userAssignments = assignmentManager.findByUser(user);

                    String rolesList = userAssignments.stream()
                            .filter(RoleAssignment::isActive)
                            .map(a -> a.role().getName())
                            .collect(Collectors.joining(", "));

                    return String.format("%-15s | %-25s | %s\n",
                            user.username(), user.fullName(), rolesList.isEmpty() ? "None" : rolesList);
                })
                .collect(Collectors.joining(""));

        sb.append(userLines);
        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ROLE USAGE REPORT ===\n");
        sb.append(String.format("%-20s | %-15s | %s\n", "Role Name", "Active Users", "Perms Count"));
        sb.append("-".repeat(60)).append("\n");

        for (Role role : roleManager.findAll()) {
            long activeUserCount = assignmentManager.findByRole(role).stream()
                    .filter(RoleAssignment::isActive)
                    .count();

            sb.append(String.format("%-20s | %-15d | %d\n",
                    role.getName(), activeUserCount, role.getPermissionCount()));
        }
        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERMISSION MATRIX ===\n");

        String userSections = userManager.findAll().parallelStream()
                .map(user -> {
                    StringBuilder userBlock = new StringBuilder();
                    userBlock.append("\nUser: ").append(user.username()).append("\n");

                    Set<Permission> permissions = assignmentManager.getUserPermissions(user);

                    if (permissions.isEmpty()) {
                        userBlock.append("  [No Active Permissions]\n");
                    } else {
                        for (Permission p : permissions) {
                            userBlock.append(String.format("  - [%s] on %s\n", p.name(), p.resource()));
                        }
                    }

                    return userBlock.toString();
                })
                .collect(Collectors.joining(""));

        sb.append(userSections);
        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        try {
            Files.writeString(Paths.get(filename), report);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
