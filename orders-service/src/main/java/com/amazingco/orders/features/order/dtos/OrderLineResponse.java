package com.amazingco.orders.features.order.dtos;

import com.amazingco.core.order.OrderLine;

import java.math.BigDecimal;

public record OrderLineResponse(String sku, int quantity, BigDecimal unitPrice, String currency) {

    public static OrderLineResponse from(OrderLine line) {
        return new OrderLineResponse(
                line.sku().value(),
                line.quantity().value(),
                line.unitPrice().amount(),
                line.unitPrice().currency().getCurrencyCode());
    }
}
