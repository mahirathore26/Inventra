package com.inventra.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull(message = "Source warehouse ID is required")
    Long sourceWarehouseId,

    @NotNull(message = "Target warehouse ID is required")
    Long targetWarehouseId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Transfer quantity must be at least 1")
    Integer quantity,

    String referenceId,
    String notes
) {}
