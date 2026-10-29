package com.selfcheckout.checkout;

import com.selfcheckout.events.KafkaTopicsConfig;
import com.selfcheckout.events.outbox.OutboxConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Checkout Service — the transaction-lifecycle microservice.
 * Owns {@code checkout_db} ({@code transactions}, {@code transaction_line_items}, {@code outbox}).
 * Serves start/scan/complete/get with no remote calls: prices come from a local catalog
 * replica built from Kafka, and scan/completion events leave via the transactional outbox.
 */
@SpringBootApplication
@Import({KafkaTopicsConfig.class, OutboxConfig.class})
public class CheckoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheckoutApplication.class, args);
    }
}
