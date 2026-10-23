-- ============================================================================
-- Inventory Service database (inventory_db).
--
-- Owned entirely by the Inventory microservice. The item `name` is denormalized
-- here so the low-stock report is served without a cross-service call. The CHECK
-- is a belt-and-suspenders backstop behind the atomic guarded UPDATE that the
-- gRPC DecrementStock issues — stock can never go negative.
-- ============================================================================

CREATE TABLE inventory_stock (
    sku   VARCHAR(20)  NOT NULL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    stock INT          NOT NULL DEFAULT 0 CHECK (stock >= 0)
);
