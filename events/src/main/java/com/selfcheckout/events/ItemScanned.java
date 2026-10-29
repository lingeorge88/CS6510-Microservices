package com.selfcheckout.events;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** One unit scanned into a basket (topic {@link Topics#ITEM_SCANNED}). */
public record ItemScanned(UUID eventId, String transactionId, String sku, String name, BigDecimal unitPrice, LocalDateTime scannedAt) {

}
