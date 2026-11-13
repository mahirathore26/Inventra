package com.inventra.warehouse.dto;

import java.time.Instant;

public record WarehouseResponse(
    Long id,
    String name,
    String code,
    String address,
    Integer capacity,
    Instant createdAt
) {}
