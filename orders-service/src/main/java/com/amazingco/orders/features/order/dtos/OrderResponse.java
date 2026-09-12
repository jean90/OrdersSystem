package com.amazingco.orders.features.order.dtos;

import com.amazingco.core.order.Order;
import com.amazingco.core.valueobject.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        BigDecimal totalAmount,
        String currency,
        List<OrderLineResponse> lines,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(Order order) {
        Money total = order.totalAmount();
        return new OrderResponse(
                order.orderId().value(),
                order.customerId().value(),
                order.status().name(),
                total != null ? total.amount() : null,
                total != null ? total.currency().getCurrencyCode() : null,
                order.lines().stream().map(OrderLineResponse::from).toList(),
                order.createdAt(),
                order.updatedAt());
    }
}
