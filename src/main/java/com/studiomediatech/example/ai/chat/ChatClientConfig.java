package com.studiomediatech.example.ai.chat;

import com.studiomediatech.example.ai.data.ServicesContextRepository;
import com.studiomediatech.example.ai.data.ServicesQueryTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
class ChatClientConfig {

    @Bean
    @DependsOn("servicesDataLoader") // the prompt embeds the schema, so the data must be loaded first
    ChatClient chatClient(ChatClient.Builder builder,
            ServicesContextRepository repository,
            ServicesQueryTool queryTool,
            @Value("${likeourowl.chat.system-prompt}") String systemPrompt) {

        String prompt = systemPrompt + """


                You can answer questions about Dutch railway services in March 2025
                by querying the DuckDB table below with the `query_services` tool.

                Table schema:
                %s

                Rules:
                - Quote every identifier in SQL: names contain spaces and colons.
                - Send exactly one read-only SELECT statement per call.
                - Results are capped at 50 rows, so aggregate instead of dumping raw rows.
                - If a query fails, read the error and retry with corrected SQL.
                """.formatted(repository.schema());

        return builder.defaultSystem(prompt).defaultTools(queryTool).build();
    }
}
