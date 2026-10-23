package com.selfcheckout.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Analytics Service — the popular-items microservice. Owns {@code analytics_db}
 * ({@code scan_events}, {@code popular_items_snapshot}). Ingests scans over gRPC
 * {@code LogScan} and serves REST {@code GET /analytics/popular-items}. The scan
 * log carries the item name, so ranking never joins another service's database.
 */
@SpringBootApplication
@EnableScheduling
public class AnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
}
