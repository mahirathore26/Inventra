package com.inventra.stock;

import com.inventra.stock.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockTransactionService stockTransactionService;

    public StockService(StockRepository stockRepository, StockTransactionService stockTransactionService) {
        this.stockRepository = stockRepository;
        this.stockTransactionService = stockTransactionService;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByWarehouse(Long warehouseId) {
        List<Stock> stocks = (warehouseId != null)
                ? stockRepository.findByWarehouseId(warehouseId)
                : stockRepository.findAll();

        return stocks.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockResponse getStockByProductAndWarehouse(Long productId, Long warehouseId) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Stock position not found for product ID " + productId + " in warehouse ID " + warehouseId));
        return toResponse(stock);
    }

    public StockResponse processReceipt(ReceiptRequest request) {
        return executeWithOptimisticRetry(() -> stockTransactionService.executeReceipt(request));
    }

    public StockResponse processAdjustment(AdjustmentRequest request) {
        return executeWithOptimisticRetry(() -> stockTransactionService.executeAdjustment(request));
    }

    public TransferResponse processTransfer(TransferRequest request) {
        return stockTransactionService.executeTransfer(request);
    }

    private <T> T executeWithOptimisticRetry(Supplier<T> action) {
        int maxAttempts = 3;
        long backoffMs = 50L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                // version conflict: retry with incremental backoff
                if (attempt == maxAttempts) {
                    throw ex;
                }
                sleepBackoff(backoffMs * attempt);
            } catch (DataIntegrityViolationException ex) {
                // race on initial row insert: retry if duplicate key, else rethrow
                if (!(ex instanceof DuplicateKeyException) || attempt == maxAttempts) {
                    throw ex;
                }
                sleepBackoff(backoffMs * attempt);
            }
        }
        throw new IllegalStateException("Unexpected exit from retry loop");
    }

    private void sleepBackoff(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Stock operation retry interrupted", ie);
        }
    }

    private StockResponse toResponse(Stock stock) {
        int available = stock.getQuantity() - stock.getReservedQuantity();
        return new StockResponse(
                stock.getId(),
                stock.getProduct().getId(),
                stock.getProduct().getSku(),
                stock.getProduct().getName(),
                stock.getWarehouse().getId(),
                stock.getWarehouse().getCode(),
                stock.getWarehouse().getName(),
                stock.getQuantity(),
                stock.getReservedQuantity(),
                available,
                stock.getMinStockLevel(),
                stock.getVersion()
        );
    }
}
