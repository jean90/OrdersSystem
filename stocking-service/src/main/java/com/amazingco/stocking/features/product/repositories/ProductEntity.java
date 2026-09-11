package com.amazingco.stocking.features.product.repositories;

import com.amazingco.core.product.Product;
import com.amazingco.core.product.ProductStatus;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

/**
 * Persistence-mapped row for the {@code product} table (see
 * {@code V1__create_product_table.sql}). Core's {@link Product} stays framework-light
 * (no Spring Data annotations), so this is the JDBC-facing shape the repository layer
 * actually reads/writes, translated to/from the domain aggregate at the boundary.
 */
@Table("product")
public record ProductEntity(
        @Id String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        String category,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    static ProductEntity fromDomain(Product product) {
        return new ProductEntity(
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

    Product toDomain() {
        return Product.reconstitute(
                new Sku(sku),
                name,
                description,
                new Money(price, Currency.getInstance(currency)),
                category,
                ProductStatus.valueOf(status),
                createdAt,
                updatedAt);
    }
}
