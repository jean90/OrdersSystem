package com.amazingco.core.order;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.OrderId;

public class OrderNotFoundException extends DomainException {

    private final OrderId orderId;

    public OrderNotFoundException(OrderId orderId) {
        super("Order not found for id: " + orderId.value());
        this.orderId = orderId;
    }

    public OrderId orderId() {
        return orderId;
    }
}
