package com.inventra.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    String description,
    BigDecimal unitCost,
    Integer reorderPoint
) {}
