CREATE TABLE stock (
    sku VARCHAR(64) PRIMARY KEY,
    available INTEGER NOT NULL CHECK (available >= 0),
    reserved INTEGER NOT NULL CHECK (reserved >= 0)
);

-- Idempotency ledger for the reserve/confirm/release saga steps, keyed by (order_id, sku):
-- a duplicate reserve command is rejected by the unique constraint below rather than
-- re-decrementing stock (see StockReservation's Javadoc).
CREATE TABLE stock_reservation (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    sku VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_stock_reservation_order_sku UNIQUE (order_id, sku)
);

CREATE INDEX idx_stock_reservation_sku ON stock_reservation (sku);
