-- ============================================================================
-- Checkout Service database (checkout_db).
--
-- Owned entirely by the Checkout microservice. Holds the transaction lifecycle.
-- Starts empty; populated at runtime as stations shop. Schema is created once by
-- the Postgres init process; the service runs ddl-auto: none.
-- ============================================================================

CREATE TABLE transactions (
    transaction_id VARCHAR(50)    NOT NULL PRIMARY KEY,
    station_id     VARCHAR(50)    NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    item_count     INT            NOT NULL,
    running_total  NUMERIC(10, 2) NOT NULL,
    started_at     TIMESTAMP      NOT NULL,
    completed_at   TIMESTAMP
);

-- One row per scanned unit. `name` is captured at scan time (from the local catalog
-- replica) so the receipt never needs to ask Catalog again.
CREATE TABLE transaction_line_items (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id VARCHAR(50)    NOT NULL,
    sku            VARCHAR(20)    NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    unit_price     NUMERIC(10, 2) NOT NULL
);
CREATE INDEX idx_line_item_transaction_id ON transaction_line_items (transaction_id);

-- Transactional outbox: events are written in the same DB transaction as the
-- business change, published to Kafka by OutboxRelay, then deleted.
CREATE TABLE outbox (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topic      VARCHAR(100) NOT NULL,
    msg_key    VARCHAR(100) NOT NULL,
    payload    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- Idempotency: ids of consumed events already applied (Kafka delivers at-least-once).
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY
);
