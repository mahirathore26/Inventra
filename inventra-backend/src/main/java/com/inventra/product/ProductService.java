package com.inventra.product;

import com.inventra.product.dto.ProductCreateRequest;
import com.inventra.product.dto.ProductResponse;
import com.inventra.product.dto.ProductUpdateRequest;
import com.inventra.stock.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public ProductService(ProductRepository productRepository, StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DataIntegrityViolationException("Product with SKU '" + request.sku() + "' already exists");
        }

        Product product = new Product(
                request.sku(),
                request.name(),
                request.description(),
                request.unitCost(),
                request.reorderPoint()
        );
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with SKU: " + sku));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setUnitCost(request.unitCost());
        product.setReorderPoint(request.reorderPoint());

        Product updated = productRepository.save(product);
        return toResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));

        if (stockRepository.existsByProductId(id)) {
            throw new DataIntegrityViolationException("Cannot delete product with ID " + id + " because active stock rows reference it");
        }

        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnitCost(),
                product.getReorderPoint()
        );
    }
}
