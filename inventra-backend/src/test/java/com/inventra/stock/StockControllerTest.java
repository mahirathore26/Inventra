package com.inventra.stock;

import com.inventra.exception.GlobalExceptionHandler;
import com.inventra.exception.InsufficientStockException;
import com.inventra.stock.dto.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@Import(GlobalExceptionHandler.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    void shouldGetStockByWarehouse() throws Exception {
        StockResponse stock = new StockResponse(
                1L, 1L, "PROD-SKU-001", "Keyboard",
                1L, "WH-CENTRAL-01", "Central DC",
                100, 10, 90, 20, 0L
        );
        when(stockService.getStockByWarehouse(1L)).thenReturn(List.of(stock));

        mockMvc.perform(get("/api/stock").param("warehouseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productSku").value("PROD-SKU-001"))
                .andExpect(jsonPath("$[0].availableQuantity").value(90));
    }

    @Test
    void shouldGetStockByProductAndWarehouse() throws Exception {
        StockResponse stock = new StockResponse(
                1L, 1L, "PROD-SKU-001", "Keyboard",
                1L, "WH-CENTRAL-01", "Central DC",
                150, 10, 140, 20, 0L
        );
        when(stockService.getStockByProductAndWarehouse(1L, 1L)).thenReturn(stock);

        mockMvc.perform(get("/api/stock/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.warehouseId").value(1))
                .andExpect(jsonPath("$.quantity").value(150))
                .andExpect(jsonPath("$.availableQuantity").value(140));
    }

    @Test
    void shouldReceiveStockSuccessfully() throws Exception {
        StockResponse stock = new StockResponse(
                1L, 1L, "PROD-SKU-001", "Keyboard",
                1L, "WH-CENTRAL-01", "Central DC",
                200, 10, 190, 20, 1L
        );
        when(stockService.processReceipt(any(ReceiptRequest.class))).thenReturn(stock);

        mockMvc.perform(post("/api/stock/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "productId": 1,
                            "warehouseId": 1,
                            "quantity": 50,
                            "referenceId": "PO-100",
                            "notes": "Intake"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(200))
                .andExpect(jsonPath("$.availableQuantity").value(190));
    }

    @Test
    void shouldAdjustStockSuccessfully() throws Exception {
        StockResponse stock = new StockResponse(
                1L, 1L, "PROD-SKU-001", "Keyboard",
                1L, "WH-CENTRAL-01", "Central DC",
                120, 10, 110, 20, 1L
        );
        when(stockService.processAdjustment(any(AdjustmentRequest.class))).thenReturn(stock);

        mockMvc.perform(post("/api/stock/adjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "productId": 1,
                            "warehouseId": 1,
                            "newQuantity": 120,
                            "reason": "Cycle count reconciliation"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(120));
    }

    @Test
    void shouldTransferStockSuccessfully() throws Exception {
        StockResponse sourceStock = new StockResponse(
                1L, 1L, "PROD-SKU-001", "Keyboard",
                1L, "WH-CENTRAL-01", "Central DC",
                100, 10, 90, 20, 1L
        );
        StockResponse targetStock = new StockResponse(
                2L, 1L, "PROD-SKU-001", "Keyboard",
                2L, "WH-WEST-02", "West DC",
                130, 0, 130, 15, 1L
        );
        TransferResponse transferResponse = new TransferResponse(sourceStock, targetStock, "TR-001");
        when(stockService.processTransfer(any(TransferRequest.class))).thenReturn(transferResponse);

        mockMvc.perform(post("/api/stock/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "productId": 1,
                            "sourceWarehouseId": 1,
                            "targetWarehouseId": 2,
                            "quantity": 50,
                            "referenceId": "TR-001",
                            "notes": "Inter-warehouse rebalancing"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceId").value("TR-001"))
                .andExpect(jsonPath("$.sourceStock.quantity").value(100))
                .andExpect(jsonPath("$.targetStock.quantity").value(130));
    }

    @Test
    void shouldReturn409WhenTransferFailsDueToInsufficientStock() throws Exception {
        when(stockService.processTransfer(any(TransferRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock in source warehouse ID 1. Available: 5, Requested: 50"));

        mockMvc.perform(post("/api/stock/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "productId": 1,
                            "sourceWarehouseId": 1,
                            "targetWarehouseId": 2,
                            "quantity": 50
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Insufficient Stock"))
                .andExpect(jsonPath("$.message").value("Insufficient stock in source warehouse ID 1. Available: 5, Requested: 50"));
    }
}
