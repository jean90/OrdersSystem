package com.amazingco.core.stock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuantityTest {

    @Test
    void rejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity(-1));
    }

    @Test
    void rejectsMinusBelowZero() {
        Quantity three = Quantity.of(3);
        Quantity five = Quantity.of(5);

        assertThrows(IllegalArgumentException.class, () -> three.minus(five));
    }

    @Test
    void addsAndSubtracts() {
        Quantity three = Quantity.of(3);
        Quantity five = Quantity.of(5);

        assertEquals(Quantity.of(8), three.plus(five));
        assertEquals(Quantity.of(2), five.minus(three));
    }

    @Test
    void rejectsOverflowOnPlus() {
        Quantity max = Quantity.of(Integer.MAX_VALUE);

        assertThrows(ArithmeticException.class, () -> max.plus(Quantity.of(1)));
    }
}
