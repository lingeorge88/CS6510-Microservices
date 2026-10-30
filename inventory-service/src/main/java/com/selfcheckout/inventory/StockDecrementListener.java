package com.selfcheckout.inventory;

import com.selfcheckout.events.StockShortfall;
import com.selfcheckout.events.Topics;
import com.selfcheckout.events.TransactionCompleted;
import com.selfcheckout.events.outbox.Outbox;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Applies completed baskets to stock. concurrency = 1 makes this the ONLY writer of
 * inventory_stock: no lock contention, no deadlocks, and a whole poll batch (up to 500
 * baskets) costs ~4 statements instead of 2 per SKU per basket. Stock is clamped at 0;
 * unfilled units are reported back as StockShortfall events via the outbox.
 */
@Component
public class StockDecrementListener {

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Outbox outbox;

    public StockDecrementListener(JdbcTemplate jdbc, JsonMapper json, Outbox outbox) {
        this.jdbc = jdbc;
        this.json = json;
        this.outbox = outbox;
    }

    @KafkaListener(topics = Topics.TRANSACTION_COMPLETED, groupId = "inventory-service",
            batch = "true", concurrency = "1")
    @Transactional
    public void onCompleted(List<String> payloads) {
        // 1. Parse; drop in-batch duplicates.
        Map<String, TransactionCompleted> byTx = new LinkedHashMap<>();
        for (String payload : payloads) {
            TransactionCompleted event = json.readValue(payload, TransactionCompleted.class);
            byTx.putIfAbsent(event.transactionId(), event);
        }

        // 2. Drop baskets already applied (redelivery): one statement for the whole batch.
        Set<String> fresh = new HashSet<>(jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO processed_transactions (transaction_id) SELECT unnest(?::varchar[])
                    ON CONFLICT DO NOTHING RETURNING transaction_id""");
            ps.setArray(1, con.createArrayOf("varchar", byTx.keySet().toArray()));
            return ps;
        }, (rs, i) -> rs.getString(1)));
        List<TransactionCompleted> events = byTx.values().stream()
                .filter(e -> fresh.contains(e.transactionId())).toList();
        if (events.isEmpty()) {
            return;
        }

        // 3. Read current stock of every touched SKU in one statement.
        Set<String> skus = new TreeSet<>();
        events.forEach(e -> e.lines().forEach(l -> skus.add(l.sku())));
        Map<String, Integer> available = new HashMap<>();
        jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT sku, stock FROM inventory_stock WHERE sku = ANY(?) ORDER BY sku FOR UPDATE");
            ps.setArray(1, con.createArrayOf("varchar", skus.toArray()));
            return ps;
        }, rs -> {
            available.put(rs.getString(1), rs.getInt(2));
        });

        // 4. Allocate in arrival order; clamp at 0; collect shortfalls.
        Map<String, Integer> decrement = new TreeMap<>();
        for (TransactionCompleted event : events) {
            List<StockShortfall.Line> shortfall = new ArrayList<>();
            for (TransactionCompleted.Line line : event.lines()) {
                int left = available.getOrDefault(line.sku(), 0);
                int fulfilled = Math.min(left, line.quantity());
                available.put(line.sku(), left - fulfilled);
                decrement.merge(line.sku(), fulfilled, Integer::sum);
                if (fulfilled < line.quantity()) {
                    shortfall.add(new StockShortfall.Line(line.sku(), line.quantity() - fulfilled));
                }
            }
            if (!shortfall.isEmpty()) {
                outbox.add(Topics.STOCK_SHORTFALL, event.transactionId(),
                        new StockShortfall(UUID.randomUUID(), event.transactionId(), shortfall));
            }
        }

        // 5. One batched UPDATE per SKU touched (CHECK stock >= 0 is the backstop).
        List<Object[]> args = decrement.entrySet().stream()
                .filter(d -> d.getValue() > 0)
                .map(d -> new Object[]{d.getValue(), d.getKey()})
                .toList();
        jdbc.batchUpdate("UPDATE inventory_stock SET stock = stock - ? WHERE sku = ?", args);
    }
}
