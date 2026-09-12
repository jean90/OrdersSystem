package com.amazingco.stocking.features.product.repositories;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The actual Spring Data JDBC adapter behind {@link ProductsRepositoryImpl}. {@code save}
 * is deliberately not used: {@code sku} is an application-assigned id, and Spring Data
 * JDBC's default insert-vs-update decision only works for DB-generated ids (it would need
 * {@code Persistable.isNew()} otherwise). An explicit upsert sidesteps that entirely.
 */
interface SpringDataProductRepository extends CrudRepository<ProductEntity, String> {

    @Modifying
    @Query("""
            INSERT INTO product (sku, name, description, price, currency, category, status, created_at, updated_at)
            VALUES (:sku, :name, :description, :price, :currency, :category, :status, :createdAt, :updatedAt)
            ON CONFLICT (sku) DO UPDATE SET
                name = EXCLUDED.name,
                description = EXCLUDED.description,
                price = EXCLUDED.price,
                currency = EXCLUDED.currency,
                category = EXCLUDED.category,
                status = EXCLUDED.status,
                updated_at = EXCLUDED.updated_at
            """)
    void upsert(@Param("sku") String sku, @Param("name") String name, @Param("description") String description,
                @Param("price") BigDecimal price, @Param("currency") String currency,
                @Param("category") String category, @Param("status") String status,
                @Param("createdAt") Instant createdAt, @Param("updatedAt") Instant updatedAt);
}
