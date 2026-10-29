package com.selfcheckout.events;

import java.util.List;
import java.util.UUID;

/** Units of a completed basket that were out of stock (topic {@link Topics#STOCK_SHORTFALL}). */
public record StockShortfall(UUID eventId, String transactionId, List<Line> lines) {
    public record Line(String sku, int unfulfilled) {
    }
}
