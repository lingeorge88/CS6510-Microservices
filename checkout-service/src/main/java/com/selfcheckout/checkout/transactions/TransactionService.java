package com.selfcheckout.checkout.transactions;

import com.selfcheckout.checkout.data.Transaction;
import com.selfcheckout.checkout.data.TransactionLineItem;
import com.selfcheckout.checkout.data.TransactionLineItemRepository;
import com.selfcheckout.checkout.data.TransactionRepository;
import com.selfcheckout.grpc.analytics.AnalyticsServiceGrpc;
import com.selfcheckout.grpc.analytics.LogScanRequest;
import com.selfcheckout.grpc.catalog.CatalogServiceGrpc;
import com.selfcheckout.grpc.catalog.GetItemRequest;
import com.selfcheckout.grpc.catalog.GetItemResponse;
import com.selfcheckout.grpc.inventory.InventoryServiceGrpc;
import com.selfcheckout.grpc.inventory.StockRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * The basket lifecycle: start, scan, inspect, complete.
 *
 * <p>This service is the <b>saga orchestrator</b>. It owns only Checkout's own
 * database (transactions + line items); everything else it needs lives in other
 * microservices and is reached over gRPC:
 * <ul>
 *   <li><b>scan</b> asks Catalog for the price/name, writes its own line item, then
 *       tells Analytics about the scan (best-effort).</li>
 *   <li><b>complete</b> drives the cross-service transaction: it decrements stock in
 *       Inventory per sold unit, then commits its own completion locally. If that
 *       local commit fails after decrements, it <b>compensates</b> by giving the
 *       units back — so stock is never silently lost and never oversold.</li>
 * </ul>
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionLineItemRepository lineItemRepository;
    private final CatalogServiceGrpc.CatalogServiceBlockingStub catalogStub;
    private final InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;
    private final AnalyticsServiceGrpc.AnalyticsServiceBlockingStub analyticsStub;
    private final TransactionTemplate txTemplate;

    private final LongAdder unfulfilledUnits = new LongAdder();

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionLineItemRepository lineItemRepository,
                              CatalogServiceGrpc.CatalogServiceBlockingStub catalogStub,
                              InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub,
                              AnalyticsServiceGrpc.AnalyticsServiceBlockingStub analyticsStub,
                              PlatformTransactionManager transactionManager) {
        this.transactionRepository = transactionRepository;
        this.lineItemRepository = lineItemRepository;
        this.catalogStub = catalogStub;
        this.inventoryStub = inventoryStub;
        this.analyticsStub = analyticsStub;
        this.txTemplate = new TransactionTemplate(transactionManager);
    }

    /** Opens a new transaction at a station. */
    @Transactional
    public Transaction start(String stationId) {
        Transaction transaction = new Transaction();
        transaction.setStationId(stationId);
        transactionRepository.save(transaction);
        return transaction;
    }

    /** @throws CheckoutException if no such transaction exists */
    public Transaction find(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> CheckoutException.transactionNotFound(transactionId));
    }

    /**
     * Scans one physical unit into the basket.
     *
     * @throws CheckoutException if the transaction is unknown or not open, or the SKU is unknown
     */
    public Map<String, Object> scan(String transactionId, String sku) {
        Transaction transaction = find(transactionId);
        if (transaction.getStatus() != Transaction.TransactionStatus.OPEN) {
            throw CheckoutException.transactionNotOpen(transactionId, transaction.getStatus());
        }

        // Catalog is the system of record for price/name — resolve it over gRPC.
        GetItemResponse item = catalogStub.getItem(GetItemRequest.newBuilder().setSku(sku).build());
        if (!item.getFound()) {
            throw CheckoutException.itemNotFound(sku);
        }
        BigDecimal unitPrice = new BigDecimal(item.getPrice());

        // Durable local write (short transaction, no remote calls held open inside it).
        Map<String, Object> result = txTemplate.execute(status -> {
            Transaction tx = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> CheckoutException.transactionNotFound(transactionId));
            if (tx.getStatus() != Transaction.TransactionStatus.OPEN) {
                throw CheckoutException.transactionNotOpen(transactionId, tx.getStatus());
            }
            lineItemRepository.save(new TransactionLineItem(transactionId, sku, unitPrice));
            tx.setItemCount(tx.getItemCount() + 1);
            tx.setRunningTotal(tx.getRunningTotal().add(unitPrice));
            transactionRepository.save(tx);
            return Map.of(
                    "transactionId", tx.getTransactionId(),
                    "sku", sku,
                    "name", item.getName(),
                    "unitPrice", unitPrice,
                    "itemCount", tx.getItemCount(),
                    "runningTotal", tx.getRunningTotal());
        });

        // Tell Analytics about the scan. Best-effort: analytics being down must
        // never fail a scan or hold up the shopper.
        logScan(sku, item.getName());

        return result;
    }

    /**
     * Completes the transaction, decrements stock across the Inventory service, and
     * returns a receipt.
     *
     * @throws CheckoutException if the transaction is unknown, not open, or empty
     */
    public Map<String, Object> complete(String transactionId) {
        Transaction transaction = find(transactionId);
        if (transaction.getStatus() != Transaction.TransactionStatus.OPEN) {
            throw CheckoutException.transactionNotOpen(transactionId, transaction.getStatus());
        }

        List<TransactionLineItem> lineItems = lineItemRepository.findByTransactionId(transactionId);
        if (lineItems.isEmpty()) {
            throw CheckoutException.basketEmpty(transactionId);
        }

        Map<String, List<TransactionLineItem>> bySku = new TreeMap<>();
        for (TransactionLineItem lineItem : lineItems) {
            bySku.computeIfAbsent(lineItem.getSku(), sku -> new ArrayList<>()).add(lineItem);
        }

        // Resolve receipt names from Catalog (one gRPC call per distinct SKU).
        Map<String, String> names = new LinkedHashMap<>();
        for (String sku : bySku.keySet()) {
            GetItemResponse item = catalogStub.getItem(GetItemRequest.newBuilder().setSku(sku).build());
            names.put(sku, item.getFound() ? item.getName() : sku);
        }

        // Saga step 1: decrement stock per unit in Inventory. Each decrement is a
        // durable, atomic, guarded operation in Inventory's own database.
        List<TransactionLineItem> fulfilled = new ArrayList<>(lineItems.size());
        List<TransactionLineItem> unfulfilled = new ArrayList<>();
        for (Map.Entry<String, List<TransactionLineItem>> entry : bySku.entrySet()) {
            for (TransactionLineItem unit : entry.getValue()) {
                boolean ok = inventoryStub.decrementStock(
                        StockRequest.newBuilder().setSku(entry.getKey()).build()).getOk();
                (ok ? fulfilled : unfulfilled).add(unit);
            }
        }

        // Saga step 2: record completion locally. If this fails after decrements,
        // compensate by returning every decremented unit to Inventory.
        BigDecimal total = BigDecimal.ZERO;
        for (TransactionLineItem lineItem : fulfilled) {
            total = total.add(lineItem.getUnitPrice());
        }
        final BigDecimal finalTotal = total;
        try {
            txTemplate.executeWithoutResult(status -> {
                if (!unfulfilled.isEmpty()) {
                    lineItemRepository.deleteAllInBatch(unfulfilled);
                }
                Transaction tx = transactionRepository.findById(transactionId)
                        .orElseThrow(() -> CheckoutException.transactionNotFound(transactionId));
                tx.setItemCount(fulfilled.size());
                tx.setRunningTotal(finalTotal);
                tx.setStatus(Transaction.TransactionStatus.COMPLETED);
                tx.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(tx);
            });
        } catch (RuntimeException e) {
            compensate(fulfilled);
            throw e;
        }

        if (!unfulfilled.isEmpty()) {
            unfulfilledUnits.add(unfulfilled.size());
        }

        Transaction completed = find(transactionId);
        return Map.of(
                "transactionId", completed.getTransactionId(),
                "stationId", completed.getStationId(),
                "itemCount", completed.getItemCount(),
                "totalAmount", completed.getRunningTotal(),
                "startedAt", completed.getStartedAt().toString(),
                "completedAt", completed.getCompletedAt().toString(),
                "lines", buildReceiptLines(fulfilled, names));
    }

    /** @return units that could not be decremented because stock had run out */
    public long unfulfilledUnits() {
        return unfulfilledUnits.sum();
    }

    /** Fire-and-forget scan notification to Analytics; failures are logged, not propagated. */
    private void logScan(String sku, String name) {
        try {
            analyticsStub.logScan(LogScanRequest.newBuilder().setSku(sku).setName(name).build());
        } catch (RuntimeException e) {
            log.warn("LogScan failed for sku {} (non-fatal): {}", sku, e.toString());
        }
    }

    /** Returns every decremented unit to Inventory after a failed local commit. */
    private void compensate(List<TransactionLineItem> fulfilled) {
        for (TransactionLineItem unit : fulfilled) {
            try {
                inventoryStub.incrementStock(StockRequest.newBuilder().setSku(unit.getSku()).build());
            } catch (RuntimeException e) {
                log.error("Compensation failed for sku {} — stock may be understated until reconciled",
                        unit.getSku(), e);
            }
        }
    }

    /** Groups sold units by SKU into receipt lines with aggregated quantities. */
    private List<Map<String, Object>> buildReceiptLines(List<TransactionLineItem> soldUnits,
                                                         Map<String, String> names) {
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (TransactionLineItem lineItem : soldUnits) {
            Map<String, Object> entry = grouped.computeIfAbsent(lineItem.getSku(), sku -> {
                Map<String, Object> newEntry = new LinkedHashMap<>();
                newEntry.put("sku", sku);
                newEntry.put("name", names.getOrDefault(sku, sku));
                newEntry.put("unitPrice", lineItem.getUnitPrice());
                newEntry.put("quantity", 0);
                return newEntry;
            });
            entry.put("quantity", (int) entry.get("quantity") + 1);
        }
        return new ArrayList<>(grouped.values());
    }
}
