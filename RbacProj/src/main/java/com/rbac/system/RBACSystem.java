package com.rbac.system;

import com.rbac.manager.AssignmentManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.UserManager;
import com.rbac.model.*;
import com.rbac.model.Permission;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final BackgroundExecutor backgroundExecutor;
    private final AuditLog auditLog;
    private final ScheduledExecutorService scheduler;

    private String currentUser;

    public RBACSystem() {
        this.assignmentManager = new AssignmentManager();
        this.roleManager = new RoleManager(this.assignmentManager);
        this.userManager = new UserManager();
        this.backgroundExecutor = new BackgroundExecutor();
        this.auditLog = new AuditLog();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RBAC-Expiration-Scheduler");
            t.setDaemon(true);
            return t;
        });
        this.currentUser = null;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public BackgroundExecutor getBackgroundExecutor() {
        return backgroundExecutor;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void startPeriodicTasks() {
        scheduler.scheduleAtFixedRate(this::runExpirationTask, 10, 30, TimeUnit.SECONDS);
    }

    private void runExpirationTask() {
        try {
            int cleanedCount = assignmentManager.cleanupExpiredTemporaryAssignments();
            String stats = generateStatistics();
            auditLog.log("PERIODIC_EXPIRATION", "system", "assignments",
                    String.format("Cleaned %d expired temporary assignments. %s", cleanedCount, stats));
        } catch (Exception e) {
            auditLog.log("PERIODIC_ERROR", "system", "assignments", "Expiration task failed: " + e.getMessage());
        }
    }

    public void shutdown() {
        backgroundExecutor.shutdown();
        auditLog.shutdown();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            this.currentUser = null;
        } else {
            this.currentUser = username.trim();
        }
    }

    public void initialize() {
        userManager.clear();
        roleManager.clear();
        assignmentManager.clear();

        Permission readUsers   = new Permission("READ",   "users",   "View list of users");
        Permission writeUsers  = new Permission("WRITE",  "users",   "Create and edit users");
        Permission deleteUsers = new Permission("DELETE", "users",   "Delete users");

        Permission readRoles   = new Permission("READ",   "roles",   "View roles");
        Permission writeRoles  = new Permission("WRITE",  "roles",   "Create and edit roles");
        Permission assignRoles = new Permission("ASSIGN", "roles",   "Assign roles to users");

        Permission readReports = new Permission("READ",   "reports", "View reports");
        Permission writeReports= new Permission("WRITE",  "reports", "Create and edit reports");

        Role adminRole = new Role("Admin", "Full access to the entire system");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(assignRoles);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);

        Role managerRole = new Role("Manager", "Content management and partial user access");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readReports);
        managerRole.addPermission(writeReports);

        Role viewerRole = new Role("Viewer", "View-only access to reports");
        viewerRole.addPermission(readReports);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        User admin = User.validate("admin", "System Administrator", "admin@company.local");
        userManager.add(admin);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial system setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(admin, adminRole, meta);
        assignmentManager.add(adminAssignment);

        setCurrentUser("admin");
        startPeriodicTasks();
    }

    public String generateStatistics() {
        int userCount = userManager.count();
        int roleCount = roleManager.count();
        int assignmentCount = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();

        StringBuilder sb = new StringBuilder();
        sb.append("=== RBAC System Status ===\n");
        sb.append("Current administrator: ").append(currentUser != null ? currentUser : "not set").append("\n");
        sb.append("Statistics date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        sb.append("Users:").append(userCount).append("\n");
        sb.append("Roles:").append(roleCount).append("\n");
        sb.append("Total role assignments:").append(assignmentCount).append("\n");
        sb.append("Active assignments:").append(activeAssignments).append("\n");
        sb.append("Expired / revoked assignments:").append(expiredAssignments).append("\n");

        return sb.toString();
    }
}
