package com.amazingco.core.valueobject;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Monetary amount in a specific currency. Uses {@link BigDecimal} rather than a floating-point
 * type to avoid rounding-error bugs in financial calculations.
 */
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Money amount must not be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Money currency must not be null");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount must not be negative: " + amount);
        }
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot operate on Money with different currencies: " + this.currency + " vs " + other.currency);
        }
    }
}
