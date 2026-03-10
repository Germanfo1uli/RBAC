package com.rbac.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    void testFormatTable() {
        String[] headers = {"ID", "Name"};
        List<String[]> rows = List.of(
                new String[]{"1", "Admin"},
                new String[]{"2", "Manager"}
        );

        String table = FormatUtils.formatTable(headers, rows);

        assertTrue(table.contains("+----+---------+"));
        assertTrue(table.contains("| ID | Name    |"));
        assertTrue(table.contains("| 1  | Admin   |"));
    }

    @Test
    void testTruncate() {
        assertEquals("Long...", FormatUtils.truncate("Long text example", 7));
        assertEquals("Short", FormatUtils.truncate("Short", 10));
    }

    @Test
    void testPadding() {
        assertEquals("Test      ", FormatUtils.padRight("Test", 10));
        assertEquals("      Test", FormatUtils.padLeft("Test", 10));
    }

    @Test
    void testFormatBox() {
        String box = FormatUtils.formatBox("Hello");
        String expected = "+-------+\n| Hello |\n+-------+";
        assertEquals(expected, box);
    }
}