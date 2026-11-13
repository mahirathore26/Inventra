package com.inventra.stock.dto;

public record StockResponse(
    Long id,
    Long productId,
    String productSku,
    String productName,
    Long warehouseId,
    String warehouseCode,
    String warehouseName,
    Integer quantity,
    Integer reservedQuantity,
    Integer availableQuantity,
    Integer minStockLevel,
    Long version
) {}
