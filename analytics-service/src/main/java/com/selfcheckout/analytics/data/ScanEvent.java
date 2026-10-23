package com.selfcheckout.analytics.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * A single item scan; its identity id doubles as the global scan sequence that
 * defines the hopping window. The item {@code name} is denormalized in from the
 * LogScan event so ranking is a pure same-database aggregation.
 */
@Entity
@Table(name = "scan_events")
public class ScanEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String sku;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt;

    protected ScanEvent() {
    }

    public ScanEvent(String sku, String name, LocalDateTime scannedAt) {
        this.sku = sku;
        this.name = name;
        this.scannedAt = scannedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }
}
