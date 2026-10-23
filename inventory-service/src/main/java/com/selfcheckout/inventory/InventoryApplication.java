package com.selfcheckout.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Inventory Service — the stock microservice. Owns {@code inventory_db}
 * ({@code inventory_stock}). Serves REST {@code GET /inventory/low-stock} and gRPC
 * {@code DecrementStock}/{@code IncrementStock} driven by Checkout's completion saga.
 */
@SpringBootApplication
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
