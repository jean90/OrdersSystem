package com.amazingco.core.stock;

import java.util.UUID;

/**
 * Typed identifier for a {@code StockReservation}, avoiding primitive obsession over raw
 * {@link UUID}.
 */
public record ReservationId(UUID value) {

    public ReservationId {
        if (value == null) {
            throw new IllegalArgumentException("ReservationId value must not be null");
        }
    }

    public static ReservationId newId() {
        return new ReservationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
