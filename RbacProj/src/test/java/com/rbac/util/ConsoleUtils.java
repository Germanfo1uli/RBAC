package com.rbac.util;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    void testPromptStringRequired() {
        Scanner scanner = createScanner("\n   \nValid Input\n");
        String result = ConsoleUtils.promptString(scanner, "Enter text", true);
        assertEquals("Valid Input", result);
    }

    @Test
    void testPromptStringOptional() {
        Scanner scanner = createScanner("\n");
        String result = ConsoleUtils.promptString(scanner, "Enter text", false);
        assertEquals("", result);
    }

    @Test
    void testPromptIntValidation() {
        Scanner scanner = createScanner("invalid\n50\n25\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter age", 18, 30);
        assertEquals(25, result);
    }

    @Test
    void testPromptYesNo() {
        Scanner scanner = createScanner("maybe\nyes\n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Confirm"));

        scanner = createScanner("no\n");
        assertFalse(ConsoleUtils.promptYesNo(scanner, "Confirm"));
    }

    @Test
    void testPromptChoice() {
        List<String> options = List.of("Option A", "Option B", "Option C");
        Scanner scanner = createScanner("5\n2\n");
        String result = ConsoleUtils.promptChoice(scanner, "Select one", options);
        assertEquals("Option B", result);
    }

    @Test
    void testPromptChoiceWithGenericObject() {
        record MockEntity(int id, String name) {
            @Override
            public String toString() { return name; }
        }

        List<MockEntity> entities = List.of(
                new MockEntity(1, "Admin"),
                new MockEntity(2, "Guest")
        );
        Scanner scanner = createScanner("1\n");
        MockEntity result = ConsoleUtils.promptChoice(scanner, "Select role", entities);

        assertEquals("Admin", result.name());
        assertEquals(1, result.id());
    }
}