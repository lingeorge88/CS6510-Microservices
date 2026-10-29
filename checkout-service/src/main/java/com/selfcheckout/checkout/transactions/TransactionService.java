package com.selfcheckout.checkout.transactions;

import com.selfcheckout.checkout.catalog.CatalogReplica;
import com.selfcheckout.checkout.data.Transaction;
import com.selfcheckout.checkout.data.TransactionRepository;
import com.selfcheckout.events.ItemScanned;
import com.selfcheckout.events.Topics;
import com.selfcheckout.events.TransactionCompleted;
import com.selfcheckout.events.outbox.Outbox;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The basket lifecycle: start, scan, inspect, complete.
 *
 * <p>Event-driven: no remote calls on any request. Prices come from the local
 * {@link CatalogReplica}; facts other services need (a scan, a completion) are written
 * to the outbox in the SAME local transaction and reach them via Kafka.
 *
 * <p>Locking rule: every write locks the {@code transactions} row FIRST (an atomic
 * {@code UPDATE ... WHERE status = 'OPEN'}), then touches line items. Concurrent scans
 * and completes on one basket serialize on that row and can never deadlock.
 */
@Service
public class TransactionService {

    private record Counters(int itemCount, BigDecimal runningTotal) {
    }

    private record Completed(String stationId, int itemCount, BigDecimal total, LocalDateTime startedAt) {
    }

    private final TransactionRepository transactionRepository;
    private final CatalogReplica catalog;
    private final JdbcTemplate jdbc;
    private final Outbox outbox;

    @PersistenceContext
    private EntityManager entityManager;

    public TransactionService(TransactionRepository transactionRepository, CatalogReplica catalog,
                              JdbcTemplate jdbc, Outbox outbox) {
        this.transactionRepository = transactionRepository;
        this.catalog = catalog;
        this.jdbc = jdbc;
        this.outbox = outbox;
    }

    /** Opens a new transaction. persist() (not save()) = one INSERT, no merge SELECT. */
    @Transactional
    public Transaction start(String stationId) {
        Transaction transaction = new Transaction();
        transaction.setStationId(stationId);
        entityManager.persist(transaction);
        return transaction;
    }

    /** @throws CheckoutException if no such transaction exists */
    public Transaction find(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> CheckoutException.transactionNotFound(transactionId));
    }

    /**
     * Scans one unit: 3 statements on one connection, zero remote calls.
     *
     * @throws CheckoutException if the transaction is unknown or not open, or the SKU is unknown
     */
    @Transactional
    public Map<String, Object> scan(String transactionId, String sku) {
        CatalogReplica.Item item = catalog.find(sku).orElse(null);
        if (item == null) {
            // Unknown transaction (404) / closed transaction (409) take precedence, as before.
            CheckoutException notOpen = notOpen(find(transactionId));
            throw notOpen != null ? notOpen : CheckoutException.itemNotFound(sku);
        }

        // Atomic increment + status guard (fixes the old lost-update race).
        List<Counters> updated = jdbc.query("""
                UPDATE transactions
                SET item_count = item_count + 1, running_total = running_total + ?
                WHERE transaction_id = ? AND status = 'OPEN'
                RETURNING item_count, running_total
                """, (rs, i) -> new Counters(rs.getInt(1), rs.getBigDecimal(2)), item.price(), transactionId);
        if (updated.isEmpty()) {
            throw notOpen(find(transactionId));
        }
        Counters counters = updated.getFirst();

        jdbc.update("INSERT INTO transaction_line_items (transaction_id, sku, name, unit_price) VALUES (?, ?, ?, ?)",
                transactionId, sku, item.name(), item.price());
        outbox.add(Topics.ITEM_SCANNED, transactionId, new ItemScanned(UUID.randomUUID(), transactionId, sku,
                item.name(), item.price(), LocalDateTime.now()));

        return Map.of(
                "transactionId", transactionId,
                "sku", sku,
                "name", item.name(),
                "unitPrice", item.price(),
                "itemCount", counters.itemCount(),
                "runningTotal", counters.runningTotal());
    }

    /**
     * Completes the basket and returns the receipt. Stock is decremented asynchronously
     * by Inventory from the {@link TransactionCompleted} event (spec: payment always
     * succeeds, no out-of-stock failure path); oversold units come back as a
     * StockShortfall event and are removed by StockShortfallListener.
     *
     * @throws CheckoutException if the transaction is unknown, not open, or empty
     */
    @Transactional
    public Map<String, Object> complete(String transactionId) {
        LocalDateTime completedAt = LocalDateTime.now();
        // Atomic OPEN -> COMPLETED (fixes the old double-complete race).
        List<Completed> rows = jdbc.query("""
                UPDATE transactions SET status = 'COMPLETED', completed_at = ?
                WHERE transaction_id = ? AND status = 'OPEN' AND item_count > 0
                RETURNING station_id, item_count, running_total, started_at
                """, (rs, i) -> new Completed(rs.getString(1), rs.getInt(2), rs.getBigDecimal(3),
                        rs.getTimestamp(4).toLocalDateTime()),
                Timestamp.valueOf(completedAt), transactionId);
        if (rows.isEmpty()) {
            Transaction tx = find(transactionId);
            CheckoutException notOpen = notOpen(tx);
            throw notOpen != null ? notOpen : CheckoutException.basketEmpty(transactionId);
        }
        Completed done = rows.getFirst();

        // Receipt lines aggregated in SQL; names were stored at scan time (no Catalog call).
        List<Map<String, Object>> lines = jdbc.query("""
                SELECT sku, name, unit_price, COUNT(*) AS quantity
                FROM transaction_line_items WHERE transaction_id = ?
                GROUP BY sku, name, unit_price ORDER BY sku
                """, (rs, i) -> {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("sku", rs.getString("sku"));
            line.put("name", rs.getString("name"));
            line.put("unitPrice", rs.getBigDecimal("unit_price"));
            line.put("quantity", rs.getInt("quantity"));
            return line;
        }, transactionId);

        outbox.add(Topics.TRANSACTION_COMPLETED, transactionId, new TransactionCompleted(UUID.randomUUID(),
                transactionId, lines.stream()
                .map(l -> new TransactionCompleted.Line((String) l.get("sku"), (int) l.get("quantity")))
                .toList()));

        return Map.of(
                "transactionId", transactionId,
                "stationId", done.stationId(),
                "itemCount", done.itemCount(),
                "totalAmount", done.total(),
                "startedAt", done.startedAt().toString(),
                "completedAt", completedAt.toString(),
                "lines", lines);
    }

    /** @return the 409 to throw if the transaction is not OPEN, else null */
    private static CheckoutException notOpen(Transaction tx) {
        return tx.getStatus() == Transaction.TransactionStatus.OPEN
                ? null
                : CheckoutException.transactionNotOpen(tx.getTransactionId(), tx.getStatus());
    }
}
