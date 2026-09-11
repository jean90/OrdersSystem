package com.amazingco.stocking.features.stock;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.Sku;

public class StockAlreadyExistsException extends DomainException {

    private final Sku sku;

    public StockAlreadyExistsException(Sku sku) {
        super("Stock already exists for SKU: " + sku.value());
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
