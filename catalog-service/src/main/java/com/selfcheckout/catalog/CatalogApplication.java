package com.selfcheckout.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Catalog Service — the product reference-data microservice. Owns {@code catalog_db}
 * ({@code catalog_items} only). Serves the REST {@code GET /items} endpoint and a
 * gRPC {@code GetItem} used by Checkout during scan and complete.
 */
@SpringBootApplication
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
