package com.selfcheckout.inventory.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Per-SKU stock level. The item {@code name} is denormalized here so the
 * low-stock report is served entirely from Inventory's own database — no
 * cross-service call to Catalog on the read path.
 */
@Entity
@Table(name = "inventory_stock")
public class InventoryStock {

    @Id
    @Column(length = 20, nullable = false)
    private String sku;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "stock", nullable = false)
    private int stock;

    protected InventoryStock() {
    }

    public InventoryStock(String sku, String name, int stock) {
        this.sku = sku;
        this.name = name;
        this.stock = stock;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }
}
