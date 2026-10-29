package com.selfcheckout.checkout.transactions;

import com.selfcheckout.events.StockShortfall;
import com.selfcheckout.events.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saga compensation (choreographed): Inventory could not fulfil some units of a
 * completed basket, so remove them from the stored transaction. Keeps
 * "stock decremented == completed line items" true once the topics drain.
 */
@Component
public class StockShortfallListener {

    private static final Logger log = LoggerFactory.getLogger(StockShortfallListener.class);

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    public StockShortfallListener(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @KafkaListener(topics = Topics.STOCK_SHORTFALL, groupId = "checkout-service")
    @Transactional
    public void onShortfall(String payload) {
        StockShortfall event = json.readValue(payload, StockShortfall.class);
        if (jdbc.update("INSERT INTO processed_events (event_id) VALUES (?) ON CONFLICT DO NOTHING",
                event.eventId()) == 0) {
            return; // redelivery, already applied
        }
        // Locking rule: transactions row first, then line items.
        jdbc.query("SELECT 1 FROM transactions WHERE transaction_id = ? FOR UPDATE",
                (rs, i) -> 1, event.transactionId());
        for (StockShortfall.Line line : event.lines()) {
            List<BigDecimal> removed = jdbc.query("""
                    DELETE FROM transaction_line_items WHERE id IN (
                        SELECT id FROM transaction_line_items
                        WHERE transaction_id = ? AND sku = ? ORDER BY id DESC LIMIT ?)
                    RETURNING unit_price
                    """, (rs, i) -> rs.getBigDecimal(1), event.transactionId(), line.sku(), line.unfulfilled());
            BigDecimal amount = removed.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            jdbc.update("UPDATE transactions SET item_count = item_count - ?, running_total = running_total - ? "
                    + "WHERE transaction_id = ?", removed.size(), amount, event.transactionId());
        }
        log.info("Shortfall applied to {}: {}", event.transactionId(), event.lines());
    }
}
