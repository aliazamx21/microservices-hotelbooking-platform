package com.paymentservice.service;

import com.paymentservice.dto.ProductRequest;
import com.paymentservice.dto.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public StripeService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public StripeResponse checkoutProducts(ProductRequest productRequest) {
        long bookingId = productRequest.getBookingId();

        // Set your secret key
        Stripe.apiKey = secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(productRequest.getName())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(productRequest.getCurrency() != null ? productRequest.getCurrency() : "USD")
                        .setUnitAmount(productRequest.getAmount())
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(productRequest.getQuantity())
                        .setPriceData(priceData)
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8080/product/v1/success?session_id={CHECKOUT_SESSION_ID}&booking_id=" + bookingId)
                        .setCancelUrl("http://localhost:8080/cancel")
                        .addLineItem(lineItem)
                        .build();

        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            System.out.println("Stripe Exception: " + e.getMessage());
        }

        StripeResponse response = new StripeResponse();
        response.setStatus("SUCCESS");
        response.setMessage("Payment session created");
        if (session != null) {
            response.setSessionId(session.getId());
            response.setSessionUrl(session.getUrl());
        }
        return response;
    }

    // --- THE SAGA PATTERN: PUBLISH EVENT TO KAFKA ---
    public boolean markBookingAsPaid(long bookingId) {
        try {
            // Publish event to Kafka. The Booking Service will listen for this.
            kafkaTemplate.send("payment-completed", String.valueOf(bookingId), String.valueOf(bookingId));
            System.out.println("Published payment-completed event to Kafka for Booking ID: " + bookingId);
            return true;
        } catch (Exception e) {
            System.err.println("Warning: Failed to publish to Kafka: " + e.getMessage());
            return false;
        }
    }
}