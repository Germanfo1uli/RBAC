package com.rbac.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
        return localDate.plusDays(days).format(DATE_FORMATTER);
    }

    public static String formatRelativeTime(String date) {
        LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
        LocalDate now = LocalDate.now();
        long days = ChronoUnit.DAYS.between(now, targetDate);

        if (days == 0) return "today";
        if (days == -1) return "1 day ago";
        if (days < 0) return Math.abs(days) + " days ago";
        if (days == 1) return "in 1 day";
        return "in " + days + " days";
    }
}