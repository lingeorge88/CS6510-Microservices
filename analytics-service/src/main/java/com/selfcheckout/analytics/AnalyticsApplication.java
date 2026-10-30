package com.selfcheckout.analytics;

import com.selfcheckout.events.KafkaTopicsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Analytics Service — the popular-items microservice. Owns {@code analytics_db}
 * ({@code scan_events}, {@code popular_items_snapshot}). Consumes
 * {@code checkout.item-scanned} events and serves REST {@code GET /analytics/popular-items}.
 * The scan log carries the item name, so ranking never joins another service's database.
 */
@SpringBootApplication
@EnableScheduling
@Import(KafkaTopicsConfig.class)
public class AnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
}
