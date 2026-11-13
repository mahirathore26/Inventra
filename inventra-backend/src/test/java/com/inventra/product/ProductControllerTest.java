package com.inventra.product;

import com.inventra.exception.GlobalExceptionHandler;
import com.inventra.product.dto.ProductCreateRequest;
import com.inventra.product.dto.ProductResponse;
import com.inventra.product.dto.ProductUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "PROD-SKU-999", "Mechanical Keyboard", "High-performance switch", new BigDecimal("75.50"), 10
        );
        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sku": "PROD-SKU-999",
                            "name": "Mechanical Keyboard",
                            "description": "High-performance switch",
                            "unitCost": 75.50,
                            "reorderPoint": 10
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("PROD-SKU-999"));
    }

    @Test
    void shouldGetProductBySku() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "PROD-SKU-001", "Keyboard", "Ergonomic", new BigDecimal("45.00"), 20
        );
        when(productService.getProductBySku("PROD-SKU-001")).thenReturn(response);

        mockMvc.perform(get("/api/products/PROD-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("PROD-SKU-001"))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void shouldReturn404WhenProductSkuNotFound() throws Exception {
        when(productService.getProductBySku("NON-EXISTENT"))
                .thenThrow(new EntityNotFoundException("Product not found with SKU: NON-EXISTENT"));

        mockMvc.perform(get("/api/products/NON-EXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with SKU: NON-EXISTENT"));
    }

    @Test
    void shouldReturn400WhenProductCostIsNegative() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sku": "SKU-INVALID",
                            "name": "Product",
                            "unitCost": -10.00,
                            "reorderPoint": -1
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.unitCost").exists())
                .andExpect(jsonPath("$.fieldErrors.reorderPoint").exists());
    }
}
