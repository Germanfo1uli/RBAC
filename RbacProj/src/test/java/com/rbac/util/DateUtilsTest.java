package com.rbac.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void testCurrentDateFormats() {
        assertTrue(DateUtils.getCurrentDate().matches("\\d{4}-\\d{2}-\\d{2}"));
        assertTrue(DateUtils.getCurrentDateTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testComparisons() {
        assertTrue(DateUtils.isBefore("2026-01-01", "2026-01-02"));
        assertFalse(DateUtils.isBefore("2026-03-09", "2026-03-09"));
        assertTrue(DateUtils.isAfter("2026-12-31", "2026-01-01"));
    }

    @Test
    void testAddDays() {
        assertEquals("2026-03-20", DateUtils.addDays("2026-03-10", 10));
        assertEquals("2026-03-01", DateUtils.addDays("2026-02-28", 1));
    }

    @Test
    void testRelativeTime() {
        String today = DateUtils.getCurrentDate();
        assertEquals("today", DateUtils.formatRelativeTime(today));
        assertEquals("1 day ago", DateUtils.formatRelativeTime(DateUtils.addDays(today, -1)));
        assertEquals("in 5 days", DateUtils.formatRelativeTime(DateUtils.addDays(today, 5)));
    }
}