package com.inventra.stock;

import com.inventra.exception.InsufficientStockException;
import com.inventra.product.Product;
import com.inventra.product.ProductRepository;
import com.inventra.stock.dto.*;
import com.inventra.warehouse.Warehouse;
import com.inventra.warehouse.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class StockTransactionService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public StockTransactionService(
            StockRepository stockRepository,
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockResponse executeReceipt(ReceiptRequest request) {
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (request.productId() == null || request.warehouseId() == null) {
            throw new IllegalArgumentException("productId and warehouseId must not be null");
        }

        Stock stock = stockRepository.findByProductIdAndWarehouseId(request.productId(), request.warehouseId())
                .orElseGet(() -> createNewStockPosition(request.productId(), request.warehouseId()));

        int quantityBefore = stock.getQuantity();
        int quantityAfter = quantityBefore + request.quantity();

        stock.setQuantity(quantityAfter);
        Stock savedStock = stockRepository.saveAndFlush(stock);

        StockMovement movement = new StockMovement(
                savedStock,
                MovementType.RECEIPT,
                request.quantity(),
                quantityBefore,
                quantityAfter,
                request.referenceId(),
                request.notes()
        );
        stockMovementRepository.save(movement);

        return toResponse(savedStock);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockResponse executeAdjustment(AdjustmentRequest request) {
        if (request.newQuantity() == null || request.newQuantity() < 0) {
            throw new IllegalArgumentException("newQuantity must be greater than or equal to 0");
        }
        if (request.productId() == null || request.warehouseId() == null) {
            throw new IllegalArgumentException("productId and warehouseId must not be null");
        }

        Stock stock = stockRepository.findByProductIdAndWarehouseId(request.productId(), request.warehouseId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Stock position not found for product ID " + request.productId() + " in warehouse ID " + request.warehouseId()));

        int quantityBefore = stock.getQuantity();
        int quantityAfter = request.newQuantity();

        if (quantityAfter < stock.getReservedQuantity()) {
            throw new InsufficientStockException(
                    "Cannot adjust stock to " + quantityAfter + " because " + stock.getReservedQuantity() + " units are currently reserved for orders");
        }

        int quantityChange = quantityAfter - quantityBefore;
        stock.setQuantity(quantityAfter);
        Stock savedStock = stockRepository.saveAndFlush(stock);

        StockMovement movement = new StockMovement(
                savedStock,
                MovementType.ADJUSTMENT,
                quantityChange,
                quantityBefore,
                quantityAfter,
                "ADJUSTMENT",
                request.reason()
        );
        stockMovementRepository.save(movement);

        return toResponse(savedStock);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse executeTransfer(TransferRequest request) {
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (request.sourceWarehouseId() == null || request.targetWarehouseId() == null) {
            throw new IllegalArgumentException("sourceWarehouseId and targetWarehouseId must not be null");
        }
        if (request.productId() == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (Objects.equals(request.sourceWarehouseId(), request.targetWarehouseId())) {
            throw new DataIntegrityViolationException("Source and target warehouses cannot be identical");
        }

        // canonical order prevents deadlock when transfers run in opposite directions
        long minWhId = Math.min(request.sourceWarehouseId(), request.targetWarehouseId());
        long maxWhId = Math.max(request.sourceWarehouseId(), request.targetWarehouseId());

        Stock minStock = (minWhId == request.sourceWarehouseId())
                ? stockRepository.findWithPessimisticLockByProductIdAndWarehouseId(request.productId(), minWhId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Stock not found in source warehouse ID " + request.sourceWarehouseId() + " for product ID " + request.productId()))
                : stockRepository.findWithPessimisticLockByProductIdAndWarehouseId(request.productId(), minWhId)
                        .orElseGet(() -> createNewStockPosition(request.productId(), minWhId));

        Stock maxStock = (maxWhId == request.sourceWarehouseId())
                ? stockRepository.findWithPessimisticLockByProductIdAndWarehouseId(request.productId(), maxWhId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Stock not found in source warehouse ID " + request.sourceWarehouseId() + " for product ID " + request.productId()))
                : stockRepository.findWithPessimisticLockByProductIdAndWarehouseId(request.productId(), maxWhId)
                        .orElseGet(() -> createNewStockPosition(request.productId(), maxWhId));

        Stock sourceStock = (minWhId == request.sourceWarehouseId()) ? minStock : maxStock;
        Stock targetStock = (minWhId == request.targetWarehouseId()) ? minStock : maxStock;

        int available = sourceStock.getQuantity() - sourceStock.getReservedQuantity();
        if (available < request.quantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock in source warehouse ID " + request.sourceWarehouseId() +
                            ". Available: " + available + ", Requested: " + request.quantity());
        }

        int sourceBefore = sourceStock.getQuantity();
        int sourceAfter = sourceBefore - request.quantity();
        sourceStock.setQuantity(sourceAfter);

        int targetBefore = targetStock.getQuantity();
        int targetAfter = targetBefore + request.quantity();
        targetStock.setQuantity(targetAfter);

        Stock savedSource = stockRepository.saveAndFlush(sourceStock);
        Stock savedTarget = stockRepository.saveAndFlush(targetStock);

        StockMovement transferOut = new StockMovement(
                savedSource,
                MovementType.TRANSFER_OUT,
                -request.quantity(),
                sourceBefore,
                sourceAfter,
                request.referenceId(),
                request.notes()
        );
        stockMovementRepository.save(transferOut);

        StockMovement transferIn = new StockMovement(
                savedTarget,
                MovementType.TRANSFER_IN,
                request.quantity(),
                targetBefore,
                targetAfter,
                request.referenceId(),
                request.notes()
        );
        stockMovementRepository.save(transferIn);

        return new TransferResponse(toResponse(savedSource), toResponse(savedTarget), request.referenceId());
    }

    private Stock createNewStockPosition(Long productId, Long warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found with ID: " + warehouseId));

        Stock newStock = new Stock(product, warehouse, 0, 0, 0);
        return stockRepository.saveAndFlush(newStock);
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
