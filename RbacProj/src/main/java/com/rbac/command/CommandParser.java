package com.rbac.command;

import com.rbac.system.RBACSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be empty");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        String normalizedName = name.trim().toLowerCase();
        commands.put(normalizedName, command);
        commandDescriptions.put(normalizedName, description != null ? description.trim() : "No description");
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        if (commandName == null || commandName.trim().isEmpty()) {
            System.out.println("No command entered.");
            return;
        }

        String normalizedName = commandName.trim().toLowerCase();
        Command command = commands.get(normalizedName);

        if (command == null) {
            System.out.println("Unknown command: " + commandName);
            System.out.println("Type 'help' to see available commands.");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Error executing command '" + commandName + "': " + e.getMessage());
        }
    }

    public void printHelp() {
        if (commands.isEmpty()) {
            System.out.println("No commands registered yet.");
            return;
        }

        System.out.println("\nAvailable commands:");
        System.out.println("--------------------------------------------------");

        commandDescriptions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String name = entry.getKey();
                    String desc = entry.getValue();
                    System.out.printf("  %-12s  %s%n", name, desc);
                });

        System.out.println("--------------------------------------------------");
        System.out.println("Type 'help' to show this message again");
        System.out.println("Type 'exit' or 'quit' to close the application (in most cases)");
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null) {
            System.out.println("No command entered.");
            return;
        }

        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return;
        }

        String[] parts = trimmedInput.split("\\s+", 2);
        String commandName = parts[0];

        executeCommand(commandName, scanner, system);
    }
}