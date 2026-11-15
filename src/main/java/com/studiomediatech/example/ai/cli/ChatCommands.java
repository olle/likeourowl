package com.studiomediatech.example.ai.cli;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.core.InputReader;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Arguments;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

/**
 * Shell commands for talking to the model.
 * <p>
 * The conversation lives in memory for as long as the shell runs, so
 * {@code ask} and {@code chat} share the same context. Answers are streamed to
 * the terminal as they are generated.
 */
@Component
@Profile("cli")
class ChatCommands {

    /**
     * Keeps the conversation from growing past the model's context window; the
     * oldest exchanges are dropped first.
     */
    private static final int MAX_HISTORY_MESSAGES = 20;

    private final ChatClient chatClient;
    private final List<Message> history = new ArrayList<>();

    ChatCommands(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Command(name = "ask", group = "LikeOurOwl", description = "Ask a single question about the railway data",
            help = "The question can be typed without quotes: ask how many stops did Utrecht Centraal have?")
    void ask(@Arguments List<String> words, CommandContext context) {
        String question = String.join(" ", words).strip();
        if (question.isEmpty()) {
            context.outputWriter().println("Usage: ask <question>");
            return;
        }
        reply(question, context.outputWriter());
    }

    @Command(name = "chat", group = "LikeOurOwl", description = "Start an interactive conversation",
            help = "Type /exit to return to the shell and /clear to forget the conversation.")
    void chat(CommandContext context) throws Exception {
        PrintWriter out = context.outputWriter();
        InputReader in = context.inputReader();

        out.println("Chat mode - '/exit' returns to the shell, '/clear' forgets the conversation.");
        while (true) {
            String line;
            try {
                line = in.readInput("you> ");
            } catch (EndOfFileException e) { // Ctrl+D
                return;
            } catch (UserInterruptException e) { // Ctrl+C
                out.println();
                return;
            }
            if (line == null) {
                return;
            }
            String input = line.strip();
            if (input.isEmpty()) {
                continue;
            }
            if (input.equalsIgnoreCase("/exit") || input.equalsIgnoreCase("/quit")) {
                return;
            }
            if (input.equalsIgnoreCase("/clear")) {
                forget(out);
                continue;
            }
            reply(input, out);
        }
    }

    @Command(name = "forget", group = "LikeOurOwl", description = "Forget the conversation so far")
    void forget(CommandContext context) {
        forget(context.outputWriter());
    }

    private void forget(PrintWriter out) {
        history.clear();
        out.println("Conversation cleared.");
        out.flush();
    }

    private void reply(String question, PrintWriter out) {
        StringBuilder answer = new StringBuilder();
        AtomicBoolean printing = new AtomicBoolean(false);

        out.print("owl> ");
        out.flush();
        try {
            chatClient.prompt()
                    .messages(history)
                    .user(question)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        // Models often start with blank lines; skip them so the
                        // answer begins right after the "owl>" prefix.
                        String text = printing.get() ? chunk : chunk.stripLeading();
                        if (text.isEmpty()) {
                            return;
                        }
                        printing.set(true);
                        out.print(text);
                        out.flush();
                        answer.append(text);
                    })
                    .blockLast();
        } catch (Exception e) {
            out.println();
            out.println("! " + rootCauseMessage(e));
            out.flush();
            return;
        }
        out.println();
        out.flush();

        remember(question, answer.toString());
    }

    private void remember(String question, String answer) {
        history.add(new UserMessage(question));
        history.add(new AssistantMessage(answer));
        while (history.size() > MAX_HISTORY_MESSAGES) {
            history.removeFirst();
        }
    }

    private static String rootCauseMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
    }
}
