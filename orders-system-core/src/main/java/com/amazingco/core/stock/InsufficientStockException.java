package com.amazingco.core.stock;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.Sku;

public class InsufficientStockException extends DomainException {

    private final Sku sku;
    private final Quantity requested;
    private final Quantity available;

    public InsufficientStockException(Sku sku, Quantity requested, Quantity available) {
        super("Insufficient stock for SKU " + sku.value() + ": requested " + requested.value()
                + " but only " + available.value() + " available");
        this.sku = sku;
        this.requested = requested;
        this.available = available;
    }

    public Sku sku() {
        return sku;
    }

    public Quantity requested() {
        return requested;
    }

    public Quantity available() {
        return available;
    }
}
