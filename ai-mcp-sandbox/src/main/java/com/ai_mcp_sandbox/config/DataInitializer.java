package com.ai_mcp_sandbox.config;

import com.ai_mcp_sandbox.service.HotelIndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(HotelIndexingService hotelIndexingService) {
        return args -> {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(5000); // Wait for Qdrant to be ready
                    log.info("Starting hotel vector indexing in background...");

                    // FIX: Provide actual hotel data to index
                    hotelIndexingService.indexHotel(new HotelIndexingService.HotelProperty(
                            "h-001", "GCP Grand Resort", "Mumbai",
                            "A luxurious 5-star hotel with cloud-native amenities and oceanic views.",
                            300.00, 4.9
                    ));

                    hotelIndexingService.indexHotel(new HotelIndexingService.HotelProperty(
                            "h-002", "Autopilot Boutique", "Bangalore",
                            "Cozy tech-friendly stay with automated room services.",
                            150.00, 4.5
                    ));

                    log.info("Hotel vector indexing completed successfully.");
                } catch (Exception e) {
                    log.error("Warning: Initial indexing failed: {}", e.getMessage());
                }
            });
        };
    }
}