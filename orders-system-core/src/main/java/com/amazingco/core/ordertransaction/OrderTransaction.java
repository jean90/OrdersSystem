package com.amazingco.core.ordertransaction;

import com.amazingco.core.valueobject.OrderId;

import java.time.Instant;

/**
 * Tracks the orchestration state of a single order's saga, one instance per {@link OrderId}
 * (see the architecture notes' {@code order_transaction} table). Distinct from {@code Order}'s
 * own customer-facing status (PENDING/CONFIRMED/CANCELLED): this is the more granular internal
 * workflow the orchestrator drives — {@code CREATED -> STOCK_RESERVED -> PAID -> CONFIRMED}, or
 * a {@code CANCELLING/CANCELLED} compensation branch. This slice only drives
 * {@code CREATED -> STOCK_RESERVED} or {@code -> CANCELLED}; no {@code markPaid()}/{@code confirm()}
 * exist yet since nothing (no payment module) drives those transitions.
 */
public class OrderTransaction {

    private final OrderId orderId;
    private OrderTransactionStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private OrderTransaction(OrderId orderId, OrderTransactionStatus status, Instant createdAt, Instant updatedAt) {
        this.orderId = orderId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderTransaction start(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        Instant now = Instant.now();
        return new OrderTransaction(orderId, OrderTransactionStatus.CREATED, now, now);
    }

    /**
     * Rehydrates an {@code OrderTransaction} from already-persisted state, preserving its actual
     * {@code status}/timestamps rather than {@link #start}'s CREATED/now defaults. For use by the
     * persistence layer only.
     */
    public static OrderTransaction reconstitute(OrderId orderId, OrderTransactionStatus status, Instant createdAt,
                                                 Instant updatedAt) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("createdAt/updatedAt must not be null");
        }
        return new OrderTransaction(orderId, status, createdAt, updatedAt);
    }

    /**
     * Idempotent: no-ops if already {@code STOCK_RESERVED}. Throws
     * {@link InvalidOrderTransactionStateException} otherwise (e.g. a late/duplicate success
     * event arriving after the transaction was already cancelled must not resurrect it).
     */
    public void markStockReserved() {
        if (status == OrderTransactionStatus.STOCK_RESERVED) {
            return;
        }
        if (status != OrderTransactionStatus.CREATED) {
            throw new InvalidOrderTransactionStateException(orderId, status, OrderTransactionStatus.STOCK_RESERVED);
        }
        status = OrderTransactionStatus.STOCK_RESERVED;
        touch();
    }

    /**
     * The compensation path — idempotent: no-ops if already {@code CANCELLED}. Allowed from
     * either {@code CREATED} or {@code STOCK_RESERVED}, mirroring {@code Order.cancel()}'s
     * permissiveness.
     */
    public void cancel() {
        if (status == OrderTransactionStatus.CANCELLED) {
            return;
        }
        status = OrderTransactionStatus.CANCELLED;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public OrderId orderId() {
        return orderId;
    }

    public OrderTransactionStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
