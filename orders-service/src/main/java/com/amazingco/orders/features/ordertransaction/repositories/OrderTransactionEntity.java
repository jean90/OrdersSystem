package com.amazingco.orders.features.ordertransaction.repositories;

import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.ordertransaction.OrderTransactionStatus;
import com.amazingco.core.valueobject.OrderId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence-mapped row for the {@code order_transaction} table (see
 * {@code V3__create_order_transaction_table.sql}). Genuinely 1:1 with an order — {@code orderId}
 * is the {@code @Id} itself, no surrogate key. Unlike {@code OrderIdempotencyKeyEntity}, plain
 * {@code save()} is correct here (not a native-insert case): this row is legitimately updated
 * multiple times as the transaction advances, not write-once.
 */
@Entity
@Table(name = "order_transaction")
public class OrderTransactionEntity {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderTransactionEntity() {
        // JPA
    }

    private OrderTransactionEntity(UUID orderId, String status, Instant createdAt, Instant updatedAt) {
        this.orderId = orderId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    static OrderTransactionEntity fromDomain(OrderTransaction transaction) {
        return new OrderTransactionEntity(transaction.orderId().value(), transaction.status().name(),
                transaction.createdAt(), transaction.updatedAt());
    }

    OrderTransaction toDomain() {
        return OrderTransaction.reconstitute(new OrderId(orderId), OrderTransactionStatus.valueOf(status),
                createdAt, updatedAt);
    }
}
