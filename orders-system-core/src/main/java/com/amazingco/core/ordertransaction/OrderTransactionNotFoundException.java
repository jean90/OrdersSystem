package com.amazingco.core.ordertransaction;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.OrderId;

public class OrderTransactionNotFoundException extends DomainException {

    private final OrderId orderId;

    public OrderTransactionNotFoundException(OrderId orderId) {
        super("Order transaction not found for order: " + orderId.value());
        this.orderId = orderId;
    }

    public OrderId orderId() {
        return orderId;
    }
}
