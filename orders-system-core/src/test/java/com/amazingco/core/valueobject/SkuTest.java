package com.amazingco.core.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkuTest {

    @Test
    void rejectsNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new Sku(null));
    }

    @Test
    void rejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new Sku("   "));
    }

    @Test
    void trimsWhitespace() {
        Sku sku = new Sku("  ABC-123  ");
        assertEquals("ABC-123", sku.value());
    }

    @Test
    void equalsByValue() {
        assertEquals(new Sku("ABC-123"), new Sku("ABC-123"));
    }
}
