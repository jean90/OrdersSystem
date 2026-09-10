package com.amazingco.stocking.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank String category) {
}
