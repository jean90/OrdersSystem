package com.amazingco.orders.features.order.dtos;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.CustomerId;

import java.util.List;

public final class CreateOrderCommand extends Command {

    private final CustomerId customerId;
    private final List<OrderLineCommand> lines;
    private final String idempotencyKey;

    public CreateOrderCommand(String traceId, CustomerId customerId, List<OrderLineCommand> lines,
                               String idempotencyKey) {
        super(traceId);
        this.customerId = customerId;
        this.lines = lines;
        this.idempotencyKey = idempotencyKey;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public List<OrderLineCommand> lines() {
        return lines;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }
}
