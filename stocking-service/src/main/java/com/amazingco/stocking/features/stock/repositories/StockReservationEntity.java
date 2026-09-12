package com.amazingco.stocking.features.stock.repositories;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.ReservationId;
import com.amazingco.core.stock.ReservationStatus;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence-mapped row for the {@code stock_reservation} table (see
 * {@code V2__create_stock_tables.sql}) — the idempotency ledger, keyed by (order_id, sku).
 */
@Table("stock_reservation")
public record StockReservationEntity(
        @Id UUID id,
        UUID orderId,
        String sku,
        int quantity,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    static StockReservationEntity fromDomain(StockReservation reservation) {
        return new StockReservationEntity(
                reservation.id().value(),
                reservation.orderId().value(),
                reservation.sku().value(),
                reservation.quantity().value(),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.updatedAt());
    }

    StockReservation toDomain() {
        return StockReservation.reconstitute(
                new ReservationId(id),
                new OrderId(orderId),
                new Sku(sku),
                Quantity.of(quantity),
                ReservationStatus.valueOf(status),
                createdAt,
                updatedAt);
    }
}
