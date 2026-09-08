package com.amazingco.core.stock;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.Sku;

public class StockNotFoundException extends DomainException {

    private final Sku sku;

    public StockNotFoundException(Sku sku) {
        super("Stock not found for SKU: " + sku.value());
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
