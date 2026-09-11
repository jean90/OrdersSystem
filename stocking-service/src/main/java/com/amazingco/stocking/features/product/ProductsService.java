package com.amazingco.stocking.features.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.product.ProductNotFoundException;
import com.amazingco.core.valueobject.Sku;

import java.util.List;

/**
 * Aggregate service port for {@link Product}: CRUD operations plus the validation
 * shared across the use-case layer (e.g. "product must exist" is enforced once by
 * {@link #findBySku(Sku)} and reused by every use case that loads a product before
 * mutating it). Takes/returns the {@link Product} aggregate itself rather than its
 * individual fields — building or mutating that aggregate from a command's values is
 * the use case's job; this port only deals in whole aggregates.
 */
public interface ProductsService {

    /**
     * Persists a new product. The caller (a use case) has already built it via
     * {@link Product#create}. Throws {@link ProductAlreadyExistsException} if a
     * product for this SKU is already registered.
     */
    Product create(Product product);

    /**
     * Throws {@link ProductNotFoundException} if no product exists for this SKU.
     */
    Product findBySku(Sku sku);

    /**
     * Persists changes to a product the caller has already loaded (via
     * {@link #findBySku(Sku)}) and mutated. Does not re-check existence.
     */
    Product update(Product product);

    List<Product> findAll();

    /**
     * Throws {@link ProductNotFoundException} if no product exists for this SKU.
     */
    void delete(Sku sku);
}
