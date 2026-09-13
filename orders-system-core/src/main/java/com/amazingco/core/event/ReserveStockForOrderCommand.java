package com.amazingco.core.event;

import java.util.List;
import java.util.UUID;

/**
 * Published by {@code orders-service}'s order-transaction orchestrator to
 * {@link Topics#RESERVE_STOCK_COMMANDS} when an order is created; consumed by
 * {@code stocking-service} to reserve every line's stock. Named {@code ...ForOrderCommand}
 * rather than {@code ReserveStockCommand} to avoid colliding with stocking-service's own local
 * per-line use-case command of that name — the listener imports both.
 */
public record ReserveStockForOrderCommand(String traceId, UUID orderId, List<StockReservationLineItem> lines) {
}
