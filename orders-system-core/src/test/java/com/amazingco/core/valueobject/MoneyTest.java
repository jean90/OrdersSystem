package com.amazingco.core.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("-1.00", "USD"));
    }

    @Test
    void addsAmountsInSameCurrency() {
        Money a = Money.of("10.00", "USD");
        Money b = Money.of("5.50", "USD");

        assertEquals(Money.of("15.50", "USD"), a.add(b));
    }

    @Test
    void subtractsAmountsInSameCurrency() {
        Money a = Money.of("10.00", "USD");
        Money b = Money.of("4.00", "USD");

        assertEquals(Money.of("6.00", "USD"), a.subtract(b));
    }

    @Test
    void rejectsCurrencyMismatch() {
        Money usd = Money.of("10.00", "USD");
        Money eur = Money.of("10.00", "EUR");

        assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
        assertThrows(IllegalArgumentException.class, () -> usd.subtract(eur));
    }

    @Test
    void equalsAndHashCodeAreScaleInsensitive() {
        // BigDecimal.equals() treats 9.99 and 9.9900 as different (scale is part of its
        // equality) even though they're the same amount - e.g. after a round-trip through a
        // fixed-scale NUMERIC(19,4) column. Money must not inherit that behavior.
        Money a = Money.of("9.99", "USD");
        Money b = new Money(new BigDecimal("9.9900"), a.currency());

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
