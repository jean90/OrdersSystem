package com.amazingco.stocking.features.product.repositories;

import com.amazingco.core.product.Product;
import com.amazingco.core.valueobject.Sku;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for {@link Product}. No implementation yet — kept as a plain
 * interface so {@link ProductsService} and the use-case layer are fully unit-testable
 * against a mock. The real implementation will be a Spring Data JDBC repository backed
 * by its own persistence-mapped representation (core's {@code Product} stays
 * framework-light).
 */
public interface ProductsRepository {

    Product save(Product product);

    Optional<Product> findBySku(Sku sku);

    boolean existsBySku(Sku sku);

    List<Product> findAll();

    void deleteBySku(Sku sku);
}
