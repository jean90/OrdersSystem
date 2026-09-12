package com.amazingco.stocking.features.stock.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ReserveStockRequest(
        @NotNull UUID orderId,
        @NotNull @Positive Integer quantity) {
}
