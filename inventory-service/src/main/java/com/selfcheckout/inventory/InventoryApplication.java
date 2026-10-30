package com.selfcheckout.inventory;

import com.selfcheckout.events.KafkaTopicsConfig;
import com.selfcheckout.events.outbox.OutboxConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Inventory Service — the stock microservice. Owns {@code inventory_db}
 * ({@code inventory_stock}, {@code processed_transactions}, {@code outbox}). Serves REST
 * {@code GET /inventory/low-stock}; consumes {@code checkout.transaction-completed} to
 * decrement stock, and publishes {@code inventory.stock-shortfall} via the outbox.
 */
@SpringBootApplication
@Import({KafkaTopicsConfig.class, OutboxConfig.class})
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
