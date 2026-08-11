package com.bookingservice.service;

import com.bookingservice.entity.Bookings; // Updated to match your exact entity name
import com.bookingservice.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);
    private final BookingRepository bookingRepository;

    // Inject the Repository directly instead of a Service
    public BookingEventListener(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // --- THE SAGA PATTERN: CONSUME PAYMENT EVENT ---
    @KafkaListener(topics = "payment-completed", groupId = "hotelbooking-group")
    public void handlePaymentCompletedEvent(String bookingIdStr) {
        logger.info("Kafka Listener received payment-completed event for Booking ID: {}", bookingIdStr);

        try {
            long bookingId = Long.parseLong(bookingIdStr);

            // Fetch the booking directly from the database using your 'Bookings' entity
            Optional<Bookings> optionalBooking = bookingRepository.findById(bookingId);

            if (optionalBooking.isPresent()) {
                Bookings booking = optionalBooking.get();

                // --- IDEMPOTENCY CHECK ---
                // Using your exact getStatus() method
                if ("CONFIRMED".equals(booking.getStatus())) {
                    logger.info("⚠️ Booking {} was already CONFIRMED. Ignoring duplicate event.", bookingId);
                    return;
                }

                // Using your exact setStatus() method
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);

                logger.info("✅ SAGA Pattern Complete: Booking {} marked as CONFIRMED in database.", bookingId);
            } else {
                logger.warn("⚠️ Received payment event for Booking {}, but it was not found in the database.", bookingId);
            }

        } catch (Exception e) {
            logger.error("❌ Error processing payment-completed event for Booking ID {}: {}", bookingIdStr, e.getMessage());
        }
    }
}