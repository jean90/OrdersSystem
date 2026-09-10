package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.valueobject.Sku;

import java.util.Optional;

/**
 * Persistence port for {@link Stock}. Deliberately shaped around atomic conditional
 * updates rather than load/mutate/save — per the concurrency architecture note, the
 * real implementation must do e.g.
 * {@code UPDATE stock SET available = available - :qty WHERE sku = :sku AND available >= :qty}
 * and report whether a row was affected, not read-modify-write the aggregate in
 * application code. No implementation yet; kept as a plain interface for now so
 * {@link StockService} is unit-testable against a mock.
 */
public interface StockRepository {

    /**
     * Atomically decrements {@code available} and increments {@code reserved} by
     * {@code quantity}, only if enough is available. Returns {@code false} (no rows
     * affected) if the SKU is unknown or {@code available < quantity} — the caller
     * can't tell which from this alone and should fall back to {@link #findBySku} to
     * build a precise error.
     */
    boolean tryReserve(Sku sku, Quantity quantity);

    /**
     * Atomically inserts a new stock row with {@code available = initialAvailable} and
     * {@code reserved = 0}. Returns {@code false} (no row inserted) if a stock row for this
     * SKU already exists — mirrors {@link #tryReserve}'s rows-affected contract rather than
     * a read-then-insert check.
     */
    boolean tryInitialize(Sku sku, Quantity initialAvailable);

    /**
     * Atomically decrements {@code reserved} by {@code quantity} (stock consumed,
     * never returns to {@code available}).
     */
    void confirmReservation(Sku sku, Quantity quantity);

    /**
     * Atomically decrements {@code reserved} and increments {@code available} by
     * {@code quantity} (compensation).
     */
    void releaseReservation(Sku sku, Quantity quantity);

    Optional<Stock> findBySku(Sku sku);
}
