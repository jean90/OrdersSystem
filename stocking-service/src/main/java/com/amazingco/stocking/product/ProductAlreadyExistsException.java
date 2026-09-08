package com.amazingco.stocking.product;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.Sku;

public class ProductAlreadyExistsException extends DomainException {

    private final Sku sku;

    public ProductAlreadyExistsException(Sku sku) {
        super("Product already exists for SKU: " + sku.value());
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
