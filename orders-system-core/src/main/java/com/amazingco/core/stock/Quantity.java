package com.amazingco.core.stock;

/**
 * A non-negative inventory count. Avoids primitive obsession over raw {@code int} and centralizes
 * the "never negative" invariant in one place.
 */
public record Quantity(int value) {

    public static final Quantity ZERO = new Quantity(0);

    public Quantity {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity must not be negative: " + value);
        }
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public Quantity plus(Quantity other) {
        return new Quantity(Math.addExact(this.value, other.value));
    }

    public Quantity minus(Quantity other) {
        return new Quantity(this.value - other.value);
    }

    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }
}
