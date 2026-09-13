package com.amazingco.core.ordertransaction;

import com.amazingco.core.valueobject.OrderId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTransactionTest {

    private static final OrderId ORDER_ID = OrderId.newId();

    @Test
    void startSetsCreatedStatus() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);

        assertEquals(ORDER_ID, transaction.orderId());
        assertEquals(OrderTransactionStatus.CREATED, transaction.status());
    }

    @Test
    void markStockReservedTransitionsFromCreated() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);

        transaction.markStockReserved();

        assertEquals(OrderTransactionStatus.STOCK_RESERVED, transaction.status());
    }

    @Test
    void markStockReservedIsANoOpWhenAlreadyStockReserved() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);
        transaction.markStockReserved();
        var updatedAtAfterFirstTransition = transaction.updatedAt();

        transaction.markStockReserved();

        assertEquals(OrderTransactionStatus.STOCK_RESERVED, transaction.status());
        assertEquals(updatedAtAfterFirstTransition, transaction.updatedAt());
    }

    @Test
    void markStockReservedThrowsWhenAlreadyCancelled() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);
        transaction.cancel();

        assertThrows(InvalidOrderTransactionStateException.class, transaction::markStockReserved);
    }

    @Test
    void cancelTransitionsFromCreated() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);

        transaction.cancel();

        assertEquals(OrderTransactionStatus.CANCELLED, transaction.status());
    }

    @Test
    void cancelTransitionsFromStockReserved() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);
        transaction.markStockReserved();

        transaction.cancel();

        assertEquals(OrderTransactionStatus.CANCELLED, transaction.status());
    }

    @Test
    void cancelIsANoOpWhenAlreadyCancelled() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);
        transaction.cancel();
        var updatedAtAfterFirstCancel = transaction.updatedAt();

        transaction.cancel();

        assertEquals(OrderTransactionStatus.CANCELLED, transaction.status());
        assertEquals(updatedAtAfterFirstCancel, transaction.updatedAt());
    }

    @Test
    void reconstitutePreservesPersistedStatusAndTimestamps() {
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-06-01T00:00:00Z");

        OrderTransaction transaction = OrderTransaction.reconstitute(ORDER_ID, OrderTransactionStatus.STOCK_RESERVED,
                createdAt, updatedAt);

        assertEquals(ORDER_ID, transaction.orderId());
        assertEquals(OrderTransactionStatus.STOCK_RESERVED, transaction.status());
        assertEquals(createdAt, transaction.createdAt());
        assertEquals(updatedAt, transaction.updatedAt());
    }
}
