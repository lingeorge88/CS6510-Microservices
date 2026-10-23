-- ============================================================================
-- Catalog Service database (catalog_db).
--
-- Owned entirely by the Catalog microservice. No other service connects here;
-- other services obtain catalog data only via the gRPC GetItem call. Schema is
-- created once by the Postgres init process; the service runs ddl-auto: none.
-- ============================================================================

CREATE TABLE catalog_items (
    sku   VARCHAR(20)    NOT NULL PRIMARY KEY,
    name  VARCHAR(255)   NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);
