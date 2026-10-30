package com.selfcheckout.inventory.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Read access for the inventory_stock table (Inventory's own database). Stock is
 * written only by StockDecrementListener, with explicit batched SQL.
 */
public interface InventoryStockRepository extends JpaRepository<InventoryStock, String> {

    List<InventoryStock> findByStockLessThan(int threshold);
}
