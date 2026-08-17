package com.ai_mcp_client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiManager implements CommandLineRunner {

    private final ChatClient chatClient;
    private final List<ToolCallbackProvider> toolProviders;

    // Spring Boot automatically injects the ChatClient Builder and the MCP Tools
    public AiManager(ChatClient.Builder builder, List<ToolCallbackProvider> toolProviders) {
        this.chatClient = builder.build();
        this.toolProviders = toolProviders;
    }

    @Override
    public void run(String... args) {
        System.out.println("====================================");
        System.out.println("AI: Let me check the hotel database...");

        // The AI Brain takes your prompt and provides it all available tools (including your MCP server tools)
        String response = chatClient.prompt("Find me a nice beach resort from the system.")
                .toolCallbacks(toolProviders.toArray(new ToolCallbackProvider[0]))
                .call()
                .content();

        System.out.println("AI Response: " + response);
        System.out.println("====================================");
    }
}