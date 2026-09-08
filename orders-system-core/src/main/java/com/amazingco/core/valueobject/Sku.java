package com.amazingco.core.valueobject;

/**
 * Typed stock-keeping unit identifier, avoiding primitive obsession on raw {@link String}.
 */
public record Sku(String value) {

    public Sku {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Sku value must not be blank");
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
