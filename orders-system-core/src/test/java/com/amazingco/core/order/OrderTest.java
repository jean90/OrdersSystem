package com.amazingco.core.order;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    private static final CustomerId CUSTOMER_ID = CustomerId.newId();
    private static final Sku SKU = new Sku("ABC-123");

    @Test
    void createStartsPendingWithNoLinesAndNoTotal() {
        Order order = Order.create(CUSTOMER_ID);

        assertEquals(CUSTOMER_ID, order.customerId());
        assertEquals(OrderStatus.PENDING, order.status());
        assertTrue(order.lines().isEmpty());
        assertNull(order.totalAmount());
    }

    @Test
    void addLineAppendsAndRecalculatesTotal() {
        Order order = Order.create(CUSTOMER_ID);

        order.addLine(SKU, Quantity.of(2), Money.of("9.99", "USD"));

        assertEquals(1, order.lines().size());
        assertEquals(Money.of("19.98", "USD"), order.totalAmount());
    }

    @Test
    void addingMultipleLinesSumsTotal() {
        Order order = Order.create(CUSTOMER_ID);

        order.addLine(SKU, Quantity.of(2), Money.of("9.99", "USD"));
        order.addLine(new Sku("XYZ-789"), Quantity.of(1), Money.of("5.00", "USD"));

        assertEquals(2, order.lines().size());
        assertEquals(Money.of("24.98", "USD"), order.totalAmount());
    }

    @Test
    void addLineRejectsCurrencyMismatchWithExistingLines() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD"));

        assertThrows(IllegalArgumentException.class,
                () -> order.addLine(new Sku("XYZ-789"), Quantity.of(1), Money.of("5.00", "EUR")));
    }

    @Test
    void addLineThrowsOnceOrderIsConfirmed() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD"));
        order.confirm();

        assertThrows(IllegalStateException.class,
                () -> order.addLine(new Sku("XYZ-789"), Quantity.of(1), Money.of("5.00", "USD")));
    }

    @Test
    void addLineThrowsOnceOrderIsCancelled() {
        Order order = Order.create(CUSTOMER_ID);
        order.cancel();

        assertThrows(IllegalStateException.class,
                () -> order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD")));
    }

    @Test
    void confirmRequiresAtLeastOneLine() {
        Order order = Order.create(CUSTOMER_ID);

        assertThrows(IllegalStateException.class, order::confirm);
    }

    @Test
    void confirmTransitionsFromPendingToConfirmed() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD"));

        order.confirm();

        assertEquals(OrderStatus.CONFIRMED, order.status());
    }

    @Test
    void confirmIsIdempotentWhenAlreadyConfirmed() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD"));
        order.confirm();

        order.confirm();

        assertEquals(OrderStatus.CONFIRMED, order.status());
    }

    @Test
    void confirmThrowsWhenAlreadyCancelled() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD"));
        order.cancel();

        InvalidOrderStateException exception = assertThrows(InvalidOrderStateException.class, order::confirm);

        assertEquals(OrderStatus.CANCELLED, exception.currentStatus());
        assertEquals(OrderStatus.CONFIRMED, exception.attemptedStatus());
    }

    @Test
    void cancelIsAllowedFromPending() {
        Order order = Order.create(CUSTOMER_ID);

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.status());
    }

    @Test
    void cancelIsAllowedFromConfirmedAsACompensationPath() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD"));
        order.confirm();

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.status());
    }

    @Test
    void cancelIsIdempotentWhenAlreadyCancelled() {
        Order order = Order.create(CUSTOMER_ID);
        order.cancel();

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.status());
    }

    @Test
    void reconstitutePreservesPersistedStateRatherThanDefaulting() {
        OrderId orderId = OrderId.newId();
        OrderLine line = OrderLine.of(SKU, Quantity.of(2), Money.of("9.99", "USD"));
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

        Order order = Order.reconstitute(orderId, CUSTOMER_ID, List.of(line), OrderStatus.CONFIRMED,
                Money.of("19.98", "USD"), createdAt, updatedAt);

        assertEquals(orderId, order.orderId());
        assertEquals(OrderStatus.CONFIRMED, order.status());
        assertEquals(1, order.lines().size());
        assertEquals(Money.of("19.98", "USD"), order.totalAmount());
        assertEquals(createdAt, order.createdAt());
        assertEquals(updatedAt, order.updatedAt());
    }
}
