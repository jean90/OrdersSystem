CREATE TABLE order_transaction (
    order_id UUID PRIMARY KEY REFERENCES orders (id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
