package com.inventra.warehouse;

import com.inventra.stock.StockRepository;
import com.inventra.warehouse.dto.WarehouseCreateRequest;
import com.inventra.warehouse.dto.WarehouseResponse;
import com.inventra.warehouse.dto.WarehouseUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;

    public WarehouseService(WarehouseRepository warehouseRepository, StockRepository stockRepository) {
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseCreateRequest request) {
        if (warehouseRepository.existsByCode(request.code())) {
            throw new DataIntegrityViolationException("Warehouse with code '" + request.code() + "' already exists");
        }
        Warehouse warehouse = new Warehouse(
                request.name(),
                request.code(),
                request.address(),
                request.capacity()
        );
        Warehouse saved = warehouseRepository.save(warehouse);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found with ID: " + id));
        return toResponse(warehouse);
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseUpdateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found with ID: " + id));

        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        warehouse.setCapacity(request.capacity());

        Warehouse updated = warehouseRepository.save(warehouse);
        return toResponse(updated);
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found with ID: " + id));

        if (stockRepository.existsByWarehouseId(id)) {
            throw new DataIntegrityViolationException("Cannot delete warehouse with ID " + id + " because active stock rows reference it");
        }

        warehouseRepository.delete(warehouse);
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getAddress(),
                warehouse.getCapacity(),
                warehouse.getCreatedAt()
        );
    }
}
