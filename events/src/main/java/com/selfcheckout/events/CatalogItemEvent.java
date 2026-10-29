package com.selfcheckout.events;

import java.math.BigDecimal;

/** Catalog reference data for one SKU (topic {@link Topics#CATALOG_ITEMS}). */
public record CatalogItemEvent(String sku, String name, BigDecimal price) {
}
