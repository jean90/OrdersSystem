package com.amazingco.stocking.features.stock.dtos;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

public final class ReserveStockCommand extends Command {

    private final OrderId orderId;
    private final Sku sku;
    private final Quantity quantity;

    public ReserveStockCommand(String traceId, OrderId orderId, Sku sku, Quantity quantity) {
        super(traceId);
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public OrderId orderId() {
        return orderId;
    }

    public Sku sku() {
        return sku;
    }

    public Quantity quantity() {
        return quantity;
    }
}
