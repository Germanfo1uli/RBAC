package com.rbac.util;

import java.util.ArrayList;
import java.util.List;

public class FormatUtils {

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return "";

        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, columnWidths.length); i++) {
                if (row[i] != null && row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        String separator = createSeparator(columnWidths);

        sb.append(separator).append("\n");
        sb.append(formatRow(headers, columnWidths)).append("\n");
        sb.append(separator).append("\n");

        for (String[] row : rows) {
            sb.append(formatRow(row, columnWidths)).append("\n");
        }
        sb.append(separator);

        return sb.toString();
    }

    public static String formatBox(String text) {
        int width = text.length() + 4;
        String line = "+" + "-".repeat(width - 2) + "+";
        return line + "\n| " + text + " |\n" + line;
    }

    public static String formatHeader(String text) {
        return "\n" + "=".repeat(10) + " " + text.toUpperCase() + " " + "=".repeat(10);
    }

    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        return String.format("%-" + length + "s", text != null ? text : "");
    }

    public static String padLeft(String text, int length) {
        return String.format("%" + length + "s", text != null ? text : "");
    }

    private static String createSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-").append("-".repeat(w)).append("-").append("+");
        }
        return sb.toString();
    }

    private static String formatRow(String[] row, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String val = i < row.length ? row[i] : "";
            sb.append(" ").append(padRight(val, widths[i])).append(" |");
        }
        return sb.toString();
    }
}