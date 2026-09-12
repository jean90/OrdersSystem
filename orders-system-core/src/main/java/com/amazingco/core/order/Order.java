package com.amazingco.core.order;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The order aggregate root, owned by {@code orders-service}. Lines are added incrementally via
 * {@link #addLine} (rather than passed as a fixed list to a factory) so each line's own
 * invariants — and the aggregate-level "every line shares one currency" invariant — are enforced
 * as the order is built up, not validated after the fact.
 * <p>
 * {@code status} here is the order's own customer-facing lifecycle (PENDING/CONFIRMED/CANCELLED)
 * — distinct from the more granular saga orchestration state machine (CREATED → STOCK_RESERVED →
 * PAID → CONFIRMED, or CANCELLING/CANCELLED) that a separate {@code saga_instance} tracks per the
 * architecture notes; that saga-state model isn't built yet.
 */
public class Order {

    private final OrderId orderId;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private OrderStatus status;
    private Money totalAmount;
    private final Instant createdAt;
    private Instant updatedAt;

    private Order(OrderId orderId, CustomerId customerId, List<OrderLine> lines, OrderStatus status,
                   Money totalAmount, Instant createdAt, Instant updatedAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.lines = new ArrayList<>(lines);
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(CustomerId customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        Instant now = Instant.now();
        return new Order(OrderId.newId(), customerId, List.of(), OrderStatus.PENDING, null, now, now);
    }

    /**
     * Rehydrates an {@code Order} from already-persisted state, preserving its actual
     * {@code status}/{@code totalAmount}/timestamps rather than {@link #create}'s
     * empty/PENDING/now defaults. For use by the persistence layer only.
     */
    public static Order reconstitute(OrderId orderId, CustomerId customerId, List<OrderLine> lines,
                                      OrderStatus status, Money totalAmount, Instant createdAt, Instant updatedAt) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (lines == null) {
            throw new IllegalArgumentException("lines must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("createdAt/updatedAt must not be null");
        }
        return new Order(orderId, customerId, lines, status, totalAmount, createdAt, updatedAt);
    }

    /**
     * Adds a line, recomputing {@link #totalAmount}. Throws {@link IllegalStateException} if the
     * order is no longer {@code PENDING} (lines can't be changed once confirmed/cancelled), or
     * {@link IllegalArgumentException} if {@code unitPrice} is in a different currency than the
     * order's existing lines.
     */
    public void addLine(Sku sku, Quantity quantity, Money unitPrice) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot add a line to a " + status + " order: " + orderId.value());
        }
        OrderLine line = OrderLine.of(sku, quantity, unitPrice);
        if (!lines.isEmpty() && !lines.get(0).unitPrice().currency().equals(unitPrice.currency())) {
            throw new IllegalArgumentException("Cannot mix currencies within one order: "
                    + lines.get(0).unitPrice().currency() + " vs " + unitPrice.currency());
        }
        lines.add(line);
        recalculateTotal();
        touch();
    }

    private void recalculateTotal() {
        Money total = null;
        for (OrderLine line : lines) {
            total = (total == null) ? line.lineTotal() : total.add(line.lineTotal());
        }
        this.totalAmount = total;
    }

    /**
     * Idempotent: no-ops if already {@code CONFIRMED}. Throws {@link InvalidOrderStateException}
     * if the order is {@code CANCELLED}, and {@link IllegalStateException} if it has no lines.
     */
    public void confirm() {
        if (status == OrderStatus.CONFIRMED) {
            return;
        }
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(orderId, status, OrderStatus.CONFIRMED);
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot confirm an order with no lines: " + orderId.value());
        }
        status = OrderStatus.CONFIRMED;
        touch();
    }

    /**
     * The compensation path — idempotent: no-ops if already {@code CANCELLED}. Unlike
     * {@link #confirm()}, cancellation is allowed from either {@code PENDING} or
     * {@code CONFIRMED}, since a saga can fail and need to compensate after confirmation too.
     */
    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        status = OrderStatus.CANCELLED;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public OrderId orderId() {
        return orderId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public List<OrderLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public OrderStatus status() {
        return status;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
