package com.amazingco.core.valueobject;

import java.util.UUID;

/**
 * Typed identifier for an {@code Order} aggregate, avoiding primitive obsession over raw
 * {@link String}/{@link UUID}.
 */
public record OrderId(UUID value) {

    public OrderId {
        if (value == null) {
            throw new IllegalArgumentException("OrderId value must not be null");
        }
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(String value) {
        return new OrderId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
