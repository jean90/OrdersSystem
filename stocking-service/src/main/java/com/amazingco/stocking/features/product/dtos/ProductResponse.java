package com.amazingco.stocking.features.product.dtos;

import com.amazingco.core.product.Product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        String category,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.sku().value(),
                product.name(),
                product.description(),
                product.price().amount(),
                product.price().currency().getCurrencyCode(),
                product.category(),
                product.status().name(),
                product.createdAt(),
                product.updatedAt());
    }
}
