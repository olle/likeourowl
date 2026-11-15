package com.studiomediatech.example.ai.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
class ChatController {

    private final ChatClient chatClient;

    ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    ChatReply chat(@RequestBody ChatRequest request) {
        requireMessage(request);
        String reply = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new ChatReply(reply);
    }

    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> stream(@RequestBody ChatRequest request) {
        requireMessage(request);
        return chatClient.prompt()
                .user(request.message())
                .stream()
                .content();
    }

    private static void requireMessage(ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.message())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }
    }
}
