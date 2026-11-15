package com.studiomediatech.example.ai.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,
            @Value("${likeourowl.chat.system-prompt:You are LikeOurOwl, a concise and helpful AI assistant.}") String systemPrompt) {
        return builder.defaultSystem(systemPrompt).build();
    }
}
