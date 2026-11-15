package com.studiomediatech.example.ai.cli;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.command.CommandParser;
import org.springframework.shell.core.command.CommandRegistry;
import org.springframework.shell.jline.PromptProvider;

/**
 * Shell look and feel for the terminal ('cli') profile.
 */
@Configuration
@Profile("cli")
class CliShellConfig {

    @Bean
    CommandParser commandParser(CommandRegistry commandRegistry) {
        return new ChatCommandParser(commandRegistry);
    }

    @Bean
    PromptProvider promptProvider() {
        return () -> new AttributedString("likeourowl> ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
    }

    /**
     * Prints a short welcome before the shell's own runner takes over stdin.
     */
    @Bean
    @Order(0)
    ApplicationRunner cliWelcome() {
        return args -> System.out.println("""

                LikeOurOwl - Dutch railway services, March 2025
                  chat   start a conversation (answers stream in)
                  ask    one-off question, e.g. ask how many stops did Utrecht Centraal have?
                  help   list all commands, quit to exit

                Or just type a question - anything that is not a command is sent to the model.
                """);
    }
}
