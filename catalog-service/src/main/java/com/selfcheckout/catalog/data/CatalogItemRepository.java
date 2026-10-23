package com.selfcheckout.catalog.data;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for the catalog_items table (Catalog's own database). */
public interface CatalogItemRepository extends JpaRepository<CatalogItem, String> {
}
