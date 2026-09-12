package com.amazingco.orders.features.order.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderLineRequest(
        @NotBlank String sku,
        @NotNull @Positive Integer quantity,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal unitPrice,
        @NotBlank @Size(min = 3, max = 3) String currency) {
}
