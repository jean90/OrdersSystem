package com.amazingco.stocking.features.stock.repositories;

import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

import java.util.Optional;

/**
 * Persistence port for the {@link StockReservation} idempotency ledger, keyed by
 * (orderId, sku). Unlike {@link StockRepository}, plain load/mutate/save is fine here
 * — this is a single ledger row per key, not the race-sensitive quantity counter.
 */
public interface StockReservationsRepository {

    Optional<StockReservation> findByOrderIdAndSku(OrderId orderId, Sku sku);

    StockReservation save(StockReservation reservation);
}
