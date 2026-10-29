package com.selfcheckout.catalog;

import com.selfcheckout.events.KafkaTopicsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Catalog Service — the product reference-data microservice. Owns {@code catalog_db}
 * ({@code catalog_items} only). Serves the REST {@code GET /items} endpoint and
 * publishes the full catalog to the compacted {@code catalog.items} Kafka topic, from
 * which other services (checkout) build their own local replica.
 */
@SpringBootApplication
@Import(KafkaTopicsConfig.class)
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
