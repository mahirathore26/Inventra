package com.inventra.stock.dto;

public record TransferResponse(
    StockResponse sourceStock,
    StockResponse targetStock,
    String referenceId
) {}
