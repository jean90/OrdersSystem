package com.amazingco.stocking.features.product.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank String category) {
}
