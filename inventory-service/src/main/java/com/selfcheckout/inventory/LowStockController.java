package com.selfcheckout.inventory;

import com.selfcheckout.inventory.data.InventoryStock;
import com.selfcheckout.inventory.data.InventoryStockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Serves {@code GET /inventory/low-stock} entirely from Inventory's own database. */
@RestController
public class LowStockController {

    private final InventoryStockRepository stock;
    private final int defaultThreshold;

    public LowStockController(InventoryStockRepository stock,
                             @Value("${selfcheckout.inventory.low-stock-threshold:50}") int defaultThreshold) {
        this.stock = stock;
        this.defaultThreshold = defaultThreshold;
    }

    /** A null {@code threshold} means "use the server default". */
    @GetMapping("/inventory/low-stock")
    public Map<String, Object> getLowStock(@RequestParam(required = false) Integer threshold) {
        int effective = threshold == null ? defaultThreshold : threshold;

        List<InventoryStock> low = stock.findByStockLessThan(effective);

        String triggeredAt = LocalDateTime.now().toString();
        List<Map<String, Object>> alerts = new ArrayList<>(low.size());
        for (InventoryStock s : low) {
            alerts.add(Map.of(
                    "sku", s.getSku(),
                    "name", s.getName(),
                    "currentStock", s.getStock(),
                    "threshold", effective,
                    "triggeredAt", triggeredAt));
        }

        return Map.of(
                "threshold", effective,
                "generatedAt", LocalDateTime.now().toString(),
                "alerts", alerts);
    }
}
