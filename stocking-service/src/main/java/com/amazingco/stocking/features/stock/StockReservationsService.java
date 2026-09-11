package com.amazingco.stocking.features.stock;

import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

import java.util.Optional;

/**
 * Aggregate service port for the {@link StockReservation} idempotency ledger.
 */
public interface StockReservationsService {

    Optional<StockReservation> findByOrderIdAndSku(OrderId orderId, Sku sku);

    StockReservation save(StockReservation reservation);
}
