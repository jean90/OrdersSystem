package com.amazingco.core.event;

import java.util.UUID;

/**
 * Published by {@code stocking-service} to {@link Topics#STOCK_RESERVATION_FAILED_EVENTS} when a
 * {@link ReserveStockForOrderCommand} could not be fully satisfied — after self-compensating by
 * releasing whichever lines it had already reserved. Consumed by {@code orders-service}'s
 * order-transaction orchestrator to cancel both the transaction and the order itself.
 */
public record StockReservationFailedEvent(String traceId, UUID orderId, String reason) {
}
