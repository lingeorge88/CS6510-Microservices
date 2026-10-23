package com.selfcheckout.inventory.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Data access for the inventory_stock table (Inventory's own database). */
public interface InventoryStockRepository extends JpaRepository<InventoryStock, String> {

    /**
     * Atomically decrements stock by 1, guarded so stock never goes negative.
     * This is the correctness heart of the system: under concurrent completions
     * the guarded conditional UPDATE serializes at the row, so two stations can
     * never both take the last unit.
     *
     * @return 1 if stock was decremented, 0 if the SKU was already out of stock
     */
    @Modifying
    @Query("UPDATE InventoryStock s SET s.stock = s.stock - 1 WHERE s.sku = :sku AND s.stock > 0")
    int decrementStock(@Param("sku") String sku);

    /**
     * Atomically increments stock by 1. Used only for saga compensation, to give
     * back a unit that Checkout decremented but could not record.
     *
     * @return 1 if the SKU exists and was incremented, 0 otherwise
     */
    @Modifying
    @Query("UPDATE InventoryStock s SET s.stock = s.stock + 1 WHERE s.sku = :sku")
    int incrementStock(@Param("sku") String sku);

    List<InventoryStock> findByStockLessThan(int threshold);
}
