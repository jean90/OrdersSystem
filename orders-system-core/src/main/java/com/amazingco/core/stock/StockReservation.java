package com.amazingco.core.stock;

import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

import java.time.Instant;

/**
 * Idempotency ledger entry for a stock reservation, keyed by (orderId, sku). Lets reserve/confirm
 * /release be safely replayed under at-least-once saga delivery: a duplicate reserve command is
 * rejected by the (orderId, sku) uniqueness constraint at the persistence layer, and confirm/
 * release are no-ops if already applied.
 */
public class StockReservation {

    private final ReservationId id;
    private final OrderId orderId;
    private final Sku sku;
    private final Quantity quantity;
    private ReservationStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private StockReservation(ReservationId id, OrderId orderId, Sku sku, Quantity quantity,
                              ReservationStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StockReservation create(OrderId orderId, Sku sku, Quantity quantity) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (sku == null) {
            throw new IllegalArgumentException("sku must not be null");
        }
        if (quantity == null || quantity.value() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }

        Instant now = Instant.now();
        return new StockReservation(ReservationId.newId(), orderId, sku, quantity,
                ReservationStatus.RESERVED, now, now);
    }

    /**
     * Rehydrates a {@code StockReservation} from already-persisted state, preserving its
     * actual {@code id}/{@code status}/timestamps rather than {@link #create}'s
     * new-id/RESERVED/now defaults. For use by the persistence layer only.
     */
    public static StockReservation reconstitute(ReservationId id, OrderId orderId, Sku sku, Quantity quantity,
                                                 ReservationStatus status, Instant createdAt, Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (sku == null) {
            throw new IllegalArgumentException("sku must not be null");
        }
        if (quantity == null || quantity.value() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("createdAt/updatedAt must not be null");
        }
        return new StockReservation(id, orderId, sku, quantity, status, createdAt, updatedAt);
    }

    public void confirm() {
        if (status == ReservationStatus.CONFIRMED) {
            return;
        }
        if (status == ReservationStatus.RELEASED) {
            throw new InvalidReservationStateException(id, status, ReservationStatus.CONFIRMED);
        }
        status = ReservationStatus.CONFIRMED;
        touch();
    }

    public void release() {
        if (status == ReservationStatus.RELEASED) {
            return;
        }
        if (status == ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException(id, status, ReservationStatus.RELEASED);
        }
        status = ReservationStatus.RELEASED;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public ReservationId id() {
        return id;
    }

    public OrderId orderId() {
        return orderId;
    }

    public Sku sku() {
        return sku;
    }

    public Quantity quantity() {
        return quantity;
    }

    public ReservationStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
