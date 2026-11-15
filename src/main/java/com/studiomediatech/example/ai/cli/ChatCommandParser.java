package com.studiomediatech.example.ai.cli;

import org.springframework.shell.core.command.CommandParser;
import org.springframework.shell.core.command.CommandRegistry;
import org.springframework.shell.core.command.DefaultCommandParser;
import org.springframework.shell.core.command.ParsedInput;

/**
 * Treats anything that is not a shell command as a chat question.
 * <p>
 * This lets you type {@code how many stops in total?} straight at the prompt
 * instead of having to prefix it with {@code ask}. Known commands - including
 * {@code quit}/{@code exit}, which the shell runner handles itself - are passed
 * through untouched.
 */
class ChatCommandParser implements CommandParser {

    private static final String ASK = "ask";

    private final CommandRegistry commandRegistry;
    private final CommandParser delegate;

    ChatCommandParser(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
        this.delegate = new DefaultCommandParser(commandRegistry);
    }

    @Override
    public ParsedInput parse(String input) {
        String trimmed = input.strip();
        if (isCommandName(firstWord(trimmed))) {
            return delegate.parse(trimmed);
        }
        return delegate.parse(ASK + " " + trimmed);
    }

    private boolean isCommandName(String name) {
        if (name.equalsIgnoreCase("quit") || name.equalsIgnoreCase("exit")) {
            return true; // handled by the shell runner before parsing
        }
        return commandRegistry.getCommandByName(name) != null
                || commandRegistry.getCommandByAlias(name) != null;
    }

    private static String firstWord(String input) {
        int end = 0;
        while (end < input.length() && !Character.isWhitespace(input.charAt(end))) {
            end++;
        }
        return input.substring(0, end);
    }
}
