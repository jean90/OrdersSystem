package com.amazingco.core.order;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.OrderId;

public class InvalidOrderStateException extends DomainException {

    private final OrderId orderId;
    private final OrderStatus currentStatus;
    private final OrderStatus attemptedStatus;

    public InvalidOrderStateException(OrderId orderId, OrderStatus currentStatus, OrderStatus attemptedStatus) {
        super("Cannot transition order " + orderId.value() + " from " + currentStatus + " to " + attemptedStatus);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public OrderId orderId() {
        return orderId;
    }

    public OrderStatus currentStatus() {
        return currentStatus;
    }

    public OrderStatus attemptedStatus() {
        return attemptedStatus;
    }
}
