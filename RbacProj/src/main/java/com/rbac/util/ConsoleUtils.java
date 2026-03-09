package com.rbac.util;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31;1m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(ANSI_CYAN + message + (required ? "*" : "") + ": " + ANSI_RESET);
            String input = scanner.nextLine().trim();

            if (required && input.isEmpty()) {
                System.out.println(ANSI_RED + "Error: This field is required." + ANSI_RESET);
                continue;
            }
            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(ANSI_CYAN + String.format("%s (%d-%d): ", message, min, max) + ANSI_RESET);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println(ANSI_RED + "Error: Value must be between " + min + " and " + max + "." + ANSI_RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Error: Invalid number format." + ANSI_RESET);
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(ANSI_CYAN + message + " (y/n): " + ANSI_RESET);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            System.out.println(ANSI_RED + "Error: Please enter 'y' or 'n'." + ANSI_RESET);
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be empty");
        }

        System.out.println(ANSI_YELLOW + "--- " + message + " ---" + ANSI_RESET);
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%s[%d]%s %s\n", ANSI_GREEN, (i + 1), ANSI_RESET, options.get(i).toString());
        }

        int choice = promptInt(scanner, "Select an option", 1, options.size());
        return options.get(choice - 1);
    }

    public static void printHeader(String title) {
        String border = "=".repeat(title.length() + 4);
        System.out.println(ANSI_YELLOW + border + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "| " + ANSI_RESET + title + ANSI_YELLOW + " |" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + border + ANSI_RESET);
    }
}