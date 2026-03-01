package com.rbac.command;

import com.rbac.system.RBACSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.*;

class CommandParserTest {

    private CommandParser parser;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @Test
    void shouldRegisterAndExecuteSimpleCommand() {
        parser.registerCommand("ping", "Echo test", (sc, sys) -> System.out.println("pong"));

        parser.parseAndExecute("ping", new Scanner(""), new RBACSystem());

        assertThat(output.toString()).contains("pong");
    }

    @Test
    void shouldPrintHelpWithRegisteredCommands() {
        parser.registerCommand("help", "Show help", (sc, sys) -> {});
        parser.registerCommand("exit", "Exit app", (sc, sys) -> {});

        parser.printHelp();

        String out = output.toString();
        assertThat(out).contains("help");
        assertThat(out).contains("exit");
    }

    @Test
    void shouldHandleUnknownCommandGracefully() {
        parser.parseAndExecute("xyz", new Scanner(""), new RBACSystem());

        String out = output.toString();
        assertThat(out).contains("Unknown command: xyz");
        assertThat(out).contains("Type 'help'");
    }

    @Test
    void shouldIgnoreEmptyInput() {
        parser.parseAndExecute("   ", new Scanner(""), new RBACSystem());

        assertThat(output.toString()).isEmpty();
    }
}