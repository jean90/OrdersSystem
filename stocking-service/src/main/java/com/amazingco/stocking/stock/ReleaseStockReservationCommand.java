package com.amazingco.stocking.stock;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;

public final class ReleaseStockReservationCommand extends Command {

    private final OrderId orderId;
    private final Sku sku;

    public ReleaseStockReservationCommand(String traceId, OrderId orderId, Sku sku) {
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
