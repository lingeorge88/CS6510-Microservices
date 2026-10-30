-- ============================================================================
-- Inventory Service database (inventory_db).
--
-- Owned entirely by the Inventory microservice. The item `name` is denormalized
-- here so the low-stock report is served without a cross-service call. Stock is
-- written ONLY by the single-threaded transaction-completed consumer, which clamps
-- at zero; the CHECK is a belt-and-suspenders backstop — stock can never go negative.
-- ============================================================================

CREATE TABLE inventory_stock (
    sku   VARCHAR(20)  NOT NULL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    stock INT          NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

-- Idempotency: completed transactions already applied to stock (Kafka delivers
-- at-least-once, so a redelivered TransactionCompleted must not decrement twice).
CREATE TABLE processed_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY
);

-- Transactional outbox: StockShortfall events are written in the same DB transaction
-- as the stock decrement, published to Kafka by OutboxRelay, then deleted.
CREATE TABLE outbox (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topic      VARCHAR(100) NOT NULL,
    msg_key    VARCHAR(100) NOT NULL,
    payload    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);
