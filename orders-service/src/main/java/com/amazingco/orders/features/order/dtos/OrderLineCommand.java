package com.amazingco.orders.features.order.dtos;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;

/**
 * One requested line within a {@link CreateOrderCommand} — plain input data, not yet the
 * validated {@link com.amazingco.core.order.OrderLine} domain object that
 * {@code Order.addLine()} builds.
 */
public record OrderLineCommand(Sku sku, Quantity quantity, Money unitPrice) {
}
