package com.amazingco.core.event;

import java.util.UUID;

/**
 * Published by {@code stocking-service} to {@link Topics#STOCK_RESERVED_EVENTS} once every line
 * on a {@link ReserveStockForOrderCommand} has been reserved; consumed by {@code orders-service}'s
 * order-transaction orchestrator to advance the transaction to {@code STOCK_RESERVED}.
 */
public record StockReservedEvent(String traceId, UUID orderId) {
}
