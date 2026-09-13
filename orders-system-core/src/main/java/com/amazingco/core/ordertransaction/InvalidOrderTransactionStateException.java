package com.amazingco.core.ordertransaction;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.valueobject.OrderId;

public class InvalidOrderTransactionStateException extends DomainException {

    private final OrderId orderId;
    private final OrderTransactionStatus currentStatus;
    private final OrderTransactionStatus attemptedStatus;

    public InvalidOrderTransactionStateException(OrderId orderId, OrderTransactionStatus currentStatus,
                                                  OrderTransactionStatus attemptedStatus) {
        super("Cannot transition order transaction " + orderId.value() + " from " + currentStatus
                + " to " + attemptedStatus);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public OrderId orderId() {
        return orderId;
    }

    public OrderTransactionStatus currentStatus() {
        return currentStatus;
    }

    public OrderTransactionStatus attemptedStatus() {
        return attemptedStatus;
    }
}
