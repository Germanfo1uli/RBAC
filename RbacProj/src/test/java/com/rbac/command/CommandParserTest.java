package com.rbac.command;

import com.rbac.system.RBACSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class CommandParserTest {

    private CommandParser parser;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    void shouldRegisterAndExecuteSimpleCommand() {
        parser.registerCommand("ping", "Echo test", (sc, sys) -> System.out.println("pong"));

        parser.parseAndExecute("ping", new Scanner(""), new RBACSystem());

        assertThat(outputStream.toString()).contains("pong");
    }

    @Test
    void shouldPrintHelpWithRegisteredCommands() {
        parser.registerCommand("help", "Show this help message", (sc, sys) -> {});
        parser.registerCommand("exit", "Exit the application", (sc, sys) -> {});

        parser.printHelp();

        String output = outputStream.toString();
        assertThat(output).contains("help");
        assertThat(output).contains("Show this help message");
        assertThat(output).contains("exit");
        assertThat(output).contains("Exit the application");
    }

    @Test
    void shouldHandleUnknownCommand() {
        parser.parseAndExecute("xyz", new Scanner(""), new RBACSystem());

        String output = outputStream.toString();
        assertThat(output).contains("Unknown command: xyz");
        assertThat(output).contains("Type 'help'");
    }

    @Test
    void shouldIgnoreEmptyAndWhitespaceInput() {
        parser.parseAndExecute("", new Scanner(""), new RBACSystem());
        assertThat(outputStream.toString()).isEmpty();

        outputStream.reset();
        parser.parseAndExecute("   \t  ", new Scanner(""), new RBACSystem());
        assertThat(outputStream.toString()).isEmpty();
    }

    @Test
    void shouldBeCaseInsensitiveForCommandNames() {
        parser.registerCommand("TEST", "Test command", (sc, sys) -> System.out.println("OK"));

        parser.parseAndExecute("test", new Scanner(""), new RBACSystem());
        assertThat(outputStream.toString()).contains("OK");

        outputStream.reset();
        parser.parseAndExecute("Test", new Scanner(""), new RBACSystem());
        assertThat(outputStream.toString()).contains("OK");

        outputStream.reset();
        parser.parseAndExecute("tEsT", new Scanner(""), new RBACSystem());
        assertThat(outputStream.toString()).contains("OK");
    }

    @Test
    void shouldNotExecuteWhenCommandNameIsNull() {
        parser.parseAndExecute(null, new Scanner(""), new RBACSystem());
        assertThat(outputStream.toString()).contains("No command entered.");
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
}