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

CREATE TABLE transaction_line_items (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id VARCHAR(50)    NOT NULL,
    sku            VARCHAR(20)    NOT NULL,
    unit_price     NUMERIC(10, 2) NOT NULL
);
CREATE INDEX idx_line_item_transaction_id ON transaction_line_items (transaction_id);
