package com.selfcheckout.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Checkout Service — the transaction-lifecycle microservice and saga orchestrator.
 * Owns {@code checkout_db} ({@code transactions}, {@code transaction_line_items}).
 * Serves start/scan/complete/get, and coordinates the cross-service completion saga
 * by calling Catalog, Inventory, and Analytics over gRPC.
 */
@SpringBootApplication
public class CheckoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheckoutApplication.class, args);
    }
}
