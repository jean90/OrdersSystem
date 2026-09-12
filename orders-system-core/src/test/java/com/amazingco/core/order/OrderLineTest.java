package com.amazingco.core.order;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderLineTest {

    private static final Sku SKU = new Sku("ABC-123");

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> OrderLine.of(SKU, Quantity.ZERO, Money.of("9.99", "USD")));
    }

    @Test
    void lineTotalMultipliesUnitPriceByQuantity() {
        OrderLine line = OrderLine.of(SKU, Quantity.of(3), Money.of("9.99", "USD"));

        assertEquals(Money.of("29.97", "USD"), line.lineTotal());
    }
}
