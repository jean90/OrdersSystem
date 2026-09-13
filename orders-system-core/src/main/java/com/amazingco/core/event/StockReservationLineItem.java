package com.amazingco.core.event;

import java.math.BigDecimal;

/**
 * One line's worth of reservation detail carried on {@link ReserveStockForOrderCommand}. Raw
 * types, not domain value objects — this is the shared wire format both services (de)serialize,
 * matching how the REST-boundary DTOs (e.g. {@code OrderResponse}) also use raw types rather
 * than embedding value objects directly.
 */
public record StockReservationLineItem(String sku, int quantity, BigDecimal unitPrice, String currency) {
}
