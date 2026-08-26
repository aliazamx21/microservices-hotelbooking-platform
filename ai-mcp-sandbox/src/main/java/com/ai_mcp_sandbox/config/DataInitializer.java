package com.ai_mcp_sandbox.config;

import com.ai_mcp_sandbox.service.HotelIndexingService;
import com.ai_mcp_sandbox.service.HotelIndexingService.HotelProperty;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initData(HotelIndexingService indexingService) {
        return args -> {
            indexingService.indexHotel(new HotelProperty(
                    "hotel-1",
                    "Grand Palm Resort & Spa",
                    "Goa",
                    "Luxury beachfront resort with private cabanas, sea view infinity pool, and coastal dining.",
                    220.0,
                    4.8
            ));

            indexingService.indexHotel(new HotelProperty(
                    "hotel-2",
                    "Sunset Bay Retreat",
                    "Kerala",
                    "Quiet beach resort nestled among coconut palms with direct shoreline access and ayurvedic spa.",
                    150.0,
                    4.6
            ));

            indexingService.indexHotel(new HotelProperty(
                    "hotel-3",
                    "Mountain Peak Lodge",
                    "Manali",
                    "Snow-capped mountain chalets with fireplace and hiking trails.",
                    110.0,
                    4.5
            ));

            System.out.println(">>> Sample hotel properties indexed successfully into Qdrant!");
        };
    }
}