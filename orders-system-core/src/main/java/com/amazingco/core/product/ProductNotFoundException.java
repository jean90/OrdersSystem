package com.amazingco.core.product;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.Sku;

public class ProductNotFoundException extends DomainException {

    private final Sku sku;

    public ProductNotFoundException(Sku sku) {
        super("Product not found for SKU: " + sku.value());
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
