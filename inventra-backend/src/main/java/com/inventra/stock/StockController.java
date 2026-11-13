package com.inventra.stock;

import com.inventra.stock.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> getStockByWarehouse(
            @RequestParam(required = false) Long warehouseId) {
        return ResponseEntity.ok(stockService.getStockByWarehouse(warehouseId));
    }

    @GetMapping("/{productId}/{warehouseId}")
    public ResponseEntity<StockResponse> getStockByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(stockService.getStockByProductAndWarehouse(productId, warehouseId));
    }

    @PostMapping("/receipt")
    public ResponseEntity<StockResponse> receiveStock(@Valid @RequestBody ReceiptRequest request) {
        return ResponseEntity.ok(stockService.processReceipt(request));
    }

    @PostMapping("/adjustment")
    public ResponseEntity<StockResponse> adjustStock(@Valid @RequestBody AdjustmentRequest request) {
        return ResponseEntity.ok(stockService.processAdjustment(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferStock(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(stockService.processTransfer(request));
    }
}
