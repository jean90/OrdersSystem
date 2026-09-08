package com.amazingco.core.stock;

import com.amazingco.core.valueobject.Sku;

/**
 * Inventory aggregate root, one per SKU. Tracks a two-bucket quantity model: {@code available}
 * (sellable) and {@code reserved} (held against a pending order). {@code reserve} moves quantity
 * available→reserved; {@code confirmReservation} permanently removes it from {@code reserved}
 * (consumed/shipped — never returns to available); {@code releaseReservation} (compensation)
 * moves it back reserved→available.
 * <p>
 * The real concurrent-safe persistence implementation uses atomic single-statement conditional
 * {@code UPDATE}s rather than optimistic/pessimistic locking or a distributed lock (the database
 * is the single source of truth) — see the architecture notes. These methods still exist to
 * express and unit-test the same business rule in isolation.
 */
public class Stock {

    private final Sku sku;
    private Quantity available;
    private Quantity reserved;

    private Stock(Sku sku, Quantity available, Quantity reserved) {
        this.sku = sku;
        this.available = available;
        this.reserved = reserved;
    }

    public static Stock initial(Sku sku, Quantity initialAvailable) {
        if (sku == null) {
            throw new IllegalArgumentException("sku must not be null");
        }
        if (initialAvailable == null) {
            throw new IllegalArgumentException("initialAvailable must not be null");
        }
        return new Stock(sku, initialAvailable, Quantity.ZERO);
    }

    public void reserve(Quantity quantity) {
        if (available.isLessThan(quantity)) {
            throw new InsufficientStockException(sku, quantity, available);
        }
        available = available.minus(quantity);
        reserved = reserved.plus(quantity);
    }

    public void confirmReservation(Quantity quantity) {
        reserved = reserved.minus(quantity);
    }

    public void releaseReservation(Quantity quantity) {
        reserved = reserved.minus(quantity);
        available = available.plus(quantity);
    }

    public Quantity onHand() {
        return available.plus(reserved);
    }

    public Sku sku() {
        return sku;
    }

    public Quantity available() {
        return available;
    }

    public Quantity reserved() {
        return reserved;
    }
}
