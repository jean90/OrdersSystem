package com.amazingco.orders.features.order.dtos;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.OrderId;

public final class GetOrderCommand extends Command {

    private final OrderId orderId;

    public GetOrderCommand(String traceId, OrderId orderId) {
        super(traceId);
        this.orderId = orderId;
    }

    public OrderId orderId() {
        return orderId;
    }
}
