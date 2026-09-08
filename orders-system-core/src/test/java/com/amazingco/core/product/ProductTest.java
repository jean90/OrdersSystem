package com.amazingco.core.product;

import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void createSetsActiveStatusAndTimestamps() {
        Product product = Product.create(new Sku("ABC-123"), "Widget", "A widget",
                Money.of("9.99", "USD"), "Widgets");

        assertEquals(ProductStatus.ACTIVE, product.status());
        assertNotNull(product.createdAt());
        assertEquals(product.createdAt(), product.updatedAt());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Product.create(
                new Sku("ABC-123"), "   ", "A widget", Money.of("9.99", "USD"), "Widgets"));
    }

    @Test
    void discontinueTransitionsStatusAndBumpsUpdatedAt() throws InterruptedException {
        Product product = Product.create(new Sku("ABC-123"), "Widget", "A widget",
                Money.of("9.99", "USD"), "Widgets");
        var createdAt = product.updatedAt();

        Thread.sleep(1);
        product.discontinue();

        assertEquals(ProductStatus.DISCONTINUED, product.status());
        assertNotNull(product.updatedAt());
        assertEquals(true, product.updatedAt().isAfter(createdAt));
    }

    @Test
    void discontinueTwiceIsANoOp() {
        Product product = Product.create(new Sku("ABC-123"), "Widget", "A widget",
                Money.of("9.99", "USD"), "Widgets");

        product.discontinue();
        var updatedAtAfterFirst = product.updatedAt();
        product.discontinue();

        assertEquals(ProductStatus.DISCONTINUED, product.status());
        assertEquals(updatedAtAfterFirst, product.updatedAt());
    }

    @Test
    void redescribeUpdatesDescriptionAndBumpsUpdatedAt() throws InterruptedException {
        Product product = Product.create(new Sku("ABC-123"), "Widget", "A widget",
                Money.of("9.99", "USD"), "Widgets");
        var createdAt = product.updatedAt();

        Thread.sleep(1);
        product.redescribe("An even better widget");

        assertEquals("An even better widget", product.description());
        assertEquals(true, product.updatedAt().isAfter(createdAt));
    }

    @Test
    void redescribeAllowsBlank() {
        Product product = Product.create(new Sku("ABC-123"), "Widget", "A widget",
                Money.of("9.99", "USD"), "Widgets");

        product.redescribe(null);

        assertEquals(null, product.description());
    }
}
