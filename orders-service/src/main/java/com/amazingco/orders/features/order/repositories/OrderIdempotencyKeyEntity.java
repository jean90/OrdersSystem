package com.amazingco.orders.features.order.repositories;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence-mapped row for the {@code order_idempotency_key} table (see
 * {@code V2__create_order_idempotency_key_table.sql}) — backs the {@code Idempotency-Key}
 * requirement on {@code POST /api/orders}. Has no domain-model counterpart; it's a pure
 * API-boundary replay-prevention record, not part of the {@code Order} aggregate's own state.
 * Read-only from JPA's perspective — writes go through
 * {@link SpringDataOrderIdempotencyKeyRepository#insert}'s raw SQL {@code INSERT}, not
 * {@code save()}, so this class only needs the no-arg constructor JPA hydration requires.
 */
@Entity
@Table(name = "order_idempotency_key")
public class OrderIdempotencyKeyEntity {

    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderIdempotencyKeyEntity() {
        // JPA
    }

    UUID orderId() {
        return orderId;
    }
}
