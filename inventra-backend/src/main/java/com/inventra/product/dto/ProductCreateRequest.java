package com.inventra.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateRequest(
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    String sku,

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    String description,

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.00", message = "Unit cost must be greater than or equal to 0")
    BigDecimal unitCost,

    @NotNull(message = "Reorder point is required")
    @Min(value = 0, message = "Reorder point must be greater than or equal to 0")
    Integer reorderPoint
) {}
