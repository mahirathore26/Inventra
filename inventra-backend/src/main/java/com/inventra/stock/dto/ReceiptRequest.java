package com.inventra.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceiptRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull(message = "Warehouse ID is required")
    Long warehouseId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    String referenceId,
    String notes
) {}
