package com.ai_mcp_client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiTestController {

    private final ChatClient chatClient;
    private final ObjectProvider<List<ToolCallbackProvider>> toolProvidersProvider;

    public AiTestController(ChatClient.Builder builder, ObjectProvider<List<ToolCallbackProvider>> toolProvidersProvider) {
        this.chatClient = builder.build();
        this.toolProvidersProvider = toolProvidersProvider;
    }

    @GetMapping("/search")
    public String askAi(@RequestParam(defaultValue = "Find me a nice beach resort from the system.") String prompt) {
        try {
            List<ToolCallbackProvider> toolProviders = toolProvidersProvider.getIfAvailable();
            ToolCallbackProvider[] callbacks = (toolProviders != null)
                    ? toolProviders.toArray(new ToolCallbackProvider[0])
                    : new ToolCallbackProvider[0];

            return chatClient.prompt(prompt)
                    .toolCallbacks(callbacks)
                    .call()
                    .content();
        } catch (Exception e) {
            return "Error during AI execution: " + e.getMessage();
        }
    }
}