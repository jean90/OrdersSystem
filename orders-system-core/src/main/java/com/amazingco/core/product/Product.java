package com.amazingco.core.product;

import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;

import java.time.Instant;

/**
 * Catalog aggregate root. Owns rarely-changing, cacheable product metadata — never the live
 * stock count, which lives on the separate {@code Stock} aggregate.
 */
public class Product {

    private final Sku sku;
    private String name;
    private String description;
    private Money price;
    private String category;
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(Sku sku, String name, String description, Money price, String category,
                     ProductStatus status, Instant createdAt, Instant updatedAt) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product create(Sku sku, String name, String description, Money price, String category) {
        if (sku == null) {
            throw new IllegalArgumentException("sku must not be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        requireNonBlank(name, "name");
        requireNonBlank(category, "category");

        Instant now = Instant.now();
        return new Product(sku, name, description, price, category, ProductStatus.ACTIVE, now, now);
    }

    public void rename(String newName) {
        requireNonBlank(newName, "name");
        this.name = newName;
        touch();
    }

    public void reprice(Money newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        this.price = newPrice;
        touch();
    }

    public void changeCategory(String newCategory) {
        requireNonBlank(newCategory, "category");
        this.category = newCategory;
        touch();
    }

    public void redescribe(String newDescription) {
        this.description = newDescription;
        touch();
    }

    public void discontinue() {
        if (status == ProductStatus.DISCONTINUED) {
            return;
        }
        this.status = ProductStatus.DISCONTINUED;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    public Sku sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return price;
    }

    public String category() {
        return category;
    }

    public ProductStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
