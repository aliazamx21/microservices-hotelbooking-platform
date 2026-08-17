package com.ai_mcp_sandbox.tool;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HotelSearchTool {

    private final VectorStore vectorStore;

    public HotelSearchTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * MCP Tool exposed to Gemini to perform semantic vector searches.
     */
    @Tool(description = "Search hotels based on natural language preferences, amenities, budget, or general vibe.")
    public List<String> searchHotelsVector(String query, int topK) {

        // Use modern Spring AI SearchRequest Builder API
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK > 0 ? topK : 3)
                .similarityThreshold(0.6)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        return results.stream()
                .map(Document::getText) // Use getText() instead of getContent()
                .collect(Collectors.toList());
    }
}