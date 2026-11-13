package com.inventra.warehouse;

import com.inventra.exception.GlobalExceptionHandler;
import com.inventra.warehouse.dto.WarehouseCreateRequest;
import com.inventra.warehouse.dto.WarehouseResponse;
import com.inventra.warehouse.dto.WarehouseUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
@Import(GlobalExceptionHandler.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WarehouseService warehouseService;

    @Test
    void shouldCreateWarehouseSuccessfully() throws Exception {
        WarehouseResponse response = new WarehouseResponse(
                1L, "South Hub", "WH-SOUTH-01", "123 Main St", 10000, Instant.now()
        );
        when(warehouseService.createWarehouse(any(WarehouseCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "South Hub",
                            "code": "WH-SOUTH-01",
                            "address": "123 Main St",
                            "capacity": 10000
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("WH-SOUTH-01"));
    }

    @Test
    void shouldReturn400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "",
                            "code": "",
                            "address": "",
                            "capacity": -5
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.code").exists())
                .andExpect(jsonPath("$.fieldErrors.capacity").exists());
    }

    @Test
    void shouldReturn404WhenWarehouseNotFound() throws Exception {
        when(warehouseService.getWarehouseById(99L))
                .thenThrow(new EntityNotFoundException("Warehouse not found with ID: 99"));

        mockMvc.perform(get("/api/warehouses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Warehouse not found with ID: 99"));
    }

    @Test
    void shouldReturn409WhenDeletingWarehouseWithExistingStock() throws Exception {
        doThrow(new DataIntegrityViolationException("Cannot delete warehouse with ID 1 because active stock rows reference it"))
                .when(warehouseService).deleteWarehouse(1L);

        mockMvc.perform(delete("/api/warehouses/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Cannot delete warehouse with ID 1 because active stock rows reference it"));
    }

    @Test
    void shouldReturn204OnSuccessfulDelete() throws Exception {
        doNothing().when(warehouseService).deleteWarehouse(2L);

        mockMvc.perform(delete("/api/warehouses/2"))
                .andExpect(status().isNoContent());
    }
}
