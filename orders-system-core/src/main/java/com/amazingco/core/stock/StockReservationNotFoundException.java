package com.amazingco.core.stock;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

public class StockReservationNotFoundException extends DomainException {

    private final OrderId orderId;
    private final Sku sku;

    public StockReservationNotFoundException(OrderId orderId, Sku sku) {
        super("Stock reservation not found for order " + orderId.value() + " and SKU " + sku.value());
        this.orderId = orderId;
        this.sku = sku;
    }

    public OrderId orderId() {
        return orderId;
    }

    public Sku sku() {
        return sku;
    }
}
