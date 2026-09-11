package com.amazingco.stocking.features.stock.dtos;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

public final class ConfirmStockReservationCommand extends Command {

    private final OrderId orderId;
    private final Sku sku;

    public ConfirmStockReservationCommand(String traceId, OrderId orderId, Sku sku) {
        super(traceId);
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
