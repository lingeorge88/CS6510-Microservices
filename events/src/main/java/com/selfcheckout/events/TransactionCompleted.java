package com.selfcheckout.events;

import java.util.List;
import java.util.UUID;

/** A basket was paid for; every unit must leave stock (topic {@link Topics#TRANSACTION_COMPLETED}). */
public record TransactionCompleted(UUID eventId, String transactionId, List<Line> lines) {

    public record Line(String sku, int quantity) {
    }
}