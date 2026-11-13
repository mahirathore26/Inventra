package com.inventra.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdjustmentRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull(message = "Warehouse ID is required")
    Long warehouseId,

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "New quantity must be greater than or equal to 0")
    Integer newQuantity,

    @NotBlank(message = "Reason is required for inventory adjustment")
    String reason
) {}
