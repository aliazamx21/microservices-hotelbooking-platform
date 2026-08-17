package com.ai_mcp_sandbox.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HotelIndexingService {

    private final VectorStore vectorStore;

    public HotelIndexingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public record HotelProperty(
            String id,
            String name,
            String city,
            String description,
            double pricePerNight,
            double rating
    ) {}

    /**
     * Converts a hotel property into a Vector Document and stores it in Qdrant.
     */
    public void indexHotel(HotelProperty hotel) {
        String content = String.format(
                "Hotel Name: %s. Location: %s. Description: %s. Price: $%.2f per night. Rating: %.1f stars.",
                hotel.name(), hotel.city(), hotel.description(), hotel.pricePerNight(), hotel.rating()
        );

        Map<String, Object> metadata = Map.of(
                "hotel_id", hotel.id(),
                "city", hotel.city(),
                "price", hotel.pricePerNight(),
                "rating", hotel.rating()
        );

        Document doc = new Document(hotel.id(), content, metadata);
        vectorStore.add(List.of(doc));
    }
}