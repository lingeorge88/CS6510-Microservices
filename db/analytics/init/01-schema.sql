-- ============================================================================
-- Analytics Service database (analytics_db).
--
-- Owned entirely by the Analytics microservice. The scan log carries the item
-- `name` (from the ItemScanned event), so popular-items ranking is a pure
-- same-database aggregation — no join to the catalog's database. `id` is an
-- identity column that doubles as the monotonic global scan sequence defining
-- the hopping window. `event_id` is UNIQUE so a redelivered event is ignored.
-- Starts empty. Schema created once; service ddl-auto: none.
-- ============================================================================

CREATE TABLE scan_events (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id   UUID         NOT NULL UNIQUE,
    sku        VARCHAR(20)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    scanned_at TIMESTAMP    NOT NULL
);

CREATE TABLE popular_items_snapshot (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    window_start BIGINT       NOT NULL,
    window_end   BIGINT       NOT NULL,
    computed_at  TIMESTAMP    NOT NULL,
    sku          VARCHAR(20)  NOT NULL,
    name         VARCHAR(255) NOT NULL,
    scan_count   INT          NOT NULL,
    rank         INT          NOT NULL
);
CREATE INDEX idx_snapshot_window_end_rank ON popular_items_snapshot (window_end, rank);
