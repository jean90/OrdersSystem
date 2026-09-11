package com.amazingco.stocking.features.stock.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InitializeStockRequest(
        @NotBlank String sku,
        @NotNull @PositiveOrZero Integer initialAvailable) {
}
