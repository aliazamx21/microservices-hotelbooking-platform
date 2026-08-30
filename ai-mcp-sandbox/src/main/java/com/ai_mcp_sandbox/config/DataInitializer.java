package com.ai_mcp_sandbox.config;

import com.ai_mcp_sandbox.service.HotelIndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(HotelIndexingService hotelIndexingService) {
        return args -> {
            try {
                log.info("Starting hotel vector indexing...");
                hotelIndexingService.indexHotel();
                log.info("Hotel vector indexing completed successfully.");
            } catch (Exception e) {
                log.error("Warning: Initial indexing failed on startup: {}. You can trigger indexing via POST /api/hotels/index", e.getMessage());
            }
        };
    }
}