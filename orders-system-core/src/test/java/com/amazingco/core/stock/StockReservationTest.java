package com.amazingco.core.stock;

import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockReservationTest {

    private static final OrderId ORDER_ID = OrderId.newId();
    private static final Sku SKU = new Sku("ABC-123");

    @Test
    void createSetsReservedStatusWithGeneratedId() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));

        assertNotNull(reservation.id());
        assertEquals(ReservationStatus.RESERVED, reservation.status());
        assertEquals(ORDER_ID, reservation.orderId());
        assertEquals(SKU, reservation.sku());
        assertEquals(Quantity.of(2), reservation.quantity());
    }

    @Test
    void confirmTransitionsToConfirmed() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));

        reservation.confirm();

        assertEquals(ReservationStatus.CONFIRMED, reservation.status());
    }

    @Test
    void confirmIsANoOpWhenAlreadyConfirmed() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));
        reservation.confirm();
        var updatedAtAfterFirstConfirm = reservation.updatedAt();

        reservation.confirm();

        assertEquals(ReservationStatus.CONFIRMED, reservation.status());
        assertEquals(updatedAtAfterFirstConfirm, reservation.updatedAt());
    }

    @Test
    void confirmThrowsWhenAlreadyReleased() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));
        reservation.release();

        assertThrows(InvalidReservationStateException.class, reservation::confirm);
    }

    @Test
    void releaseThrowsWhenAlreadyConfirmed() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));
        reservation.confirm();

        assertThrows(InvalidReservationStateException.class, reservation::release);
    }

    @Test
    void reconstitutePreservesPersistedIdStatusAndTimestamps() {
        ReservationId id = ReservationId.newId();
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-06-01T00:00:00Z");

        StockReservation reservation = StockReservation.reconstitute(id, ORDER_ID, SKU, Quantity.of(2),
                ReservationStatus.CONFIRMED, createdAt, updatedAt);

        assertEquals(id, reservation.id());
        assertEquals(ReservationStatus.CONFIRMED, reservation.status());
        assertEquals(createdAt, reservation.createdAt());
        assertEquals(updatedAt, reservation.updatedAt());
    }
}
