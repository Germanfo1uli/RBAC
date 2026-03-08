package com.rbac.report;

import com.rbac.manager.AssignmentManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.UserManager;
import com.rbac.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportGeneratorTest {

    private ReportGenerator reportGenerator;
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        reportGenerator = new ReportGenerator();
        userManager = mock(UserManager.class);
        roleManager = mock(RoleManager.class);
        assignmentManager = mock(AssignmentManager.class);
    }

    @Test
    void testGenerateUserReport() {
        User user = new User("admin", "Admin User", "admin@rbac.com");
        Role role = new Role("SuperAdmin", "Full access");
        RoleAssignment assignment = mock(RoleAssignment.class);

        when(userManager.findAll()).thenReturn(List.of(user));
        when(assignmentManager.findByUser(user)).thenReturn(List.of(assignment));
        when(assignment.isActive()).thenReturn(true);
        when(assignment.role()).thenReturn(role);

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertTrue(report.contains("admin"));
        assertTrue(report.contains("SuperAdmin"));
    }

    @Test
    void testGenerateRoleReport() {
        Role role = new Role("Manager", "Managerial role");
        RoleAssignment assignment = mock(RoleAssignment.class);

        when(roleManager.findAll()).thenReturn(List.of(role));
        when(assignmentManager.findByRole(role)).thenReturn(List.of(assignment));
        when(assignment.isActive()).thenReturn(true);

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertTrue(report.contains("Manager"));
        assertTrue(report.contains("1"));
    }

    @Test
    void testGeneratePermissionMatrix() {
        User user = new User("tester", "Test User", "test@rbac.com");
        Permission perm = new Permission("P1", "reports", "Read reports");

        when(userManager.findAll()).thenReturn(List.of(user));
        when(assignmentManager.getUserPermissions(user)).thenReturn(Set.of(perm));

        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertTrue(report.contains("tester"));
        assertTrue(report.contains("P1"));
        assertTrue(report.contains("reports"));
    }

    @Test
    void testExportToFile(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("report.txt");
        String content = "Report data";

        reportGenerator.exportToFile(content, filePath.toString());

        assertTrue(Files.exists(filePath));
        assertEquals(content, Files.readString(filePath));
    }
}