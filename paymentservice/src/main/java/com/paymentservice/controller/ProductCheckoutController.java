package com.paymentservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentservice.dto.ProductRequest;
import com.paymentservice.dto.StripeResponse;
import com.paymentservice.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

@RestController
@RequestMapping("/product/v1")
public class ProductCheckoutController {

    private final StripeService stripeService;

    @Value("${stripe.secretKey}")
    private String secretKey;

    public ProductCheckoutController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/checkout")
    public StripeResponse checkoutProducts(@RequestBody ProductRequest productRequest) {
        return stripeService.checkoutProducts(productRequest);
    }

    @GetMapping("/success")
    public String handleSuccess(@RequestParam("session_id") String sessionId, @RequestParam("booking_id") long id) {
        // Securely load from environment instead of hardcoding
        Stripe.apiKey = secretKey;

        try {
            Session session = Session.retrieve(sessionId);
            String paymentStatus = session.getPaymentStatus();
            System.out.println("Stripe Session ID: " + sessionId);

            if ("paid".equalsIgnoreCase(paymentStatus)) {
                System.out.println("✅ Payment successful: true");

                // --- SAGA PATTERN: Trigger Kafka Event ---
                stripeService.markBookingAsPaid(id);

                return "Payment successful";
            } else {
                System.out.println("❌ Payment not completed: false");
                return "Payment not completed";
            }

        } catch (StripeException e) {
            e.printStackTrace();
            return "Stripe error occurred";
        }
    }

    @GetMapping("/cancel")
    public String handleCancel() {
        System.out.println("❌ Payment cancelled: false");
        return "Payment cancelled";
    }
}