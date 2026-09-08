package com.amazingco.core.stock;

import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockTest {

    private static final Sku SKU = new Sku("ABC-123");

    @Test
    void initialStateHasZeroReserved() {
        Stock stock = Stock.initial(SKU, Quantity.of(10));

        assertEquals(Quantity.of(10), stock.available());
        assertEquals(Quantity.ZERO, stock.reserved());
        assertEquals(Quantity.of(10), stock.onHand());
    }

    @Test
    void reserveMovesQuantityFromAvailableToReserved() {
        Stock stock = Stock.initial(SKU, Quantity.of(10));

        stock.reserve(Quantity.of(4));

        assertEquals(Quantity.of(6), stock.available());
        assertEquals(Quantity.of(4), stock.reserved());
        assertEquals(Quantity.of(10), stock.onHand());
    }

    @Test
    void reserveWithInsufficientAvailableThrows() {
        Stock stock = Stock.initial(SKU, Quantity.of(3));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> stock.reserve(Quantity.of(5)));

        assertEquals(SKU, exception.sku());
        assertEquals(Quantity.of(5), exception.requested());
        assertEquals(Quantity.of(3), exception.available());
    }

    @Test
    void confirmReservationPermanentlyReducesOnHand() {
        Stock stock = Stock.initial(SKU, Quantity.of(10));
        stock.reserve(Quantity.of(4));

        stock.confirmReservation(Quantity.of(4));

        assertEquals(Quantity.of(6), stock.available());
        assertEquals(Quantity.ZERO, stock.reserved());
        assertEquals(Quantity.of(6), stock.onHand());
    }

    @Test
    void releaseReservationMovesQuantityBackToAvailable() {
        Stock stock = Stock.initial(SKU, Quantity.of(10));
        stock.reserve(Quantity.of(4));

        stock.releaseReservation(Quantity.of(4));

        assertEquals(Quantity.of(10), stock.available());
        assertEquals(Quantity.ZERO, stock.reserved());
        assertEquals(Quantity.of(10), stock.onHand());
    }
}
