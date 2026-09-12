-- Backs the Idempotency-Key requirement on POST /api/orders. Kept as its own table, decoupled
-- from the orders/order_line rows, rather than a column on `orders`: it's purely an API-boundary
-- replay-prevention record (looked up once, at creation, and never touched again), not part of
-- the order's own persisted state.
CREATE TABLE order_idempotency_key (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_order_idempotency_key_order_id ON order_idempotency_key (order_id);
