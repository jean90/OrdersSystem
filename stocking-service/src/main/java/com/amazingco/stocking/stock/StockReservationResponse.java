package com.amazingco.stocking.stock;

import com.amazingco.core.stock.StockReservation;

import java.time.Instant;
import java.util.UUID;

public record StockReservationResponse(
        UUID id,
        UUID orderId,
        String sku,
        int quantity,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    static StockReservationResponse from(StockReservation reservation) {
        return new StockReservationResponse(
                reservation.id().value(),
                reservation.orderId().value(),
                reservation.sku().value(),
                reservation.quantity().value(),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.updatedAt());
    }
}
