-- ============================================================================
-- Inventra Sample Dataset
-- 2 Warehouses, 5 Products, Initial Stock Positions, and Genesis Movement Records
-- ============================================================================

-- 1. Insert 2 Warehouses
INSERT INTO warehouses (name, code, address, capacity, created_at)
VALUES 
    ('Central Distribution Center', 'WH-CENTRAL-01', '100 Industrial Parkway, Chicago, IL 60601', 50000, CURRENT_TIMESTAMP),
    ('West Coast Regional Warehouse', 'WH-WEST-02', '550 Logistics Blvd, Reno, NV 89501', 30000, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 2. Insert 5 Products
INSERT INTO products (sku, name, description, unit_cost, reorder_point)
VALUES 
    ('PROD-SKU-001', 'Wireless Ergonomic Keyboard', 'Low-profile wireless mechanical keyboard with ergonomic split layout', 45.00, 20),
    ('PROD-SKU-002', 'Ultra HD Monitor 27-inch', '4K UHD IPS display with USB-C 90W power delivery', 279.50, 10),
    ('PROD-SKU-003', 'Noise-Cancelling Headphones', 'Over-ear Bluetooth headphones with hybrid active noise cancellation', 85.00, 15),
    ('PROD-SKU-004', 'USB-C Multi-Port Hub', '7-in-1 aluminum USB-C hub with 4K HDMI, SD card reader, 100W PD', 18.75, 30),
    ('PROD-SKU-005', 'High-Speed Thermal Receipt Printer', 'Direct thermal POS barcode and receipt printer with auto-cutter', 65.00, 8)
ON CONFLICT (sku) DO NOTHING;

-- 3. Insert Initial Stock Rows for Warehouse 1 (WH-CENTRAL-01)
INSERT INTO stock (product_id, warehouse_id, quantity, reserved_quantity, min_stock_level, version)
VALUES
    (1, 1, 150, 10, 20, 0),
    (2, 1, 40, 5, 10, 0),
    (3, 1, 75, 0, 15, 0),
    (4, 1, 200, 25, 30, 0),
    (5, 1, 35, 2, 8, 0)
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

-- 4. Insert Initial Stock Rows for Warehouse 2 (WH-WEST-02)
INSERT INTO stock (product_id, warehouse_id, quantity, reserved_quantity, min_stock_level, version)
VALUES
    (1, 2, 80, 0, 15, 0),
    (2, 2, 25, 2, 5, 0),
    (3, 2, 50, 4, 10, 0),
    (4, 2, 120, 10, 20, 0),
    (5, 2, 15, 0, 5, 0)
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

-- 5. Insert Initial Receipt Movements in Append-Only Ledger for Audit Trail
INSERT INTO stock_movements (product_id, warehouse_id, movement_type, quantity_change, quantity_before, quantity_after, reference_id, notes, created_at)
VALUES
    (1, 1, 'RECEIPT', 150, 0, 150, 'PO-2026-001', 'Initial warehouse inventory intake', CURRENT_TIMESTAMP),
    (2, 1, 'RECEIPT', 40, 0, 40, 'PO-2026-001', 'Initial warehouse inventory intake', CURRENT_TIMESTAMP),
    (3, 1, 'RECEIPT', 75, 0, 75, 'PO-2026-001', 'Initial warehouse inventory intake', CURRENT_TIMESTAMP),
    (4, 1, 'RECEIPT', 200, 0, 200, 'PO-2026-001', 'Initial warehouse inventory intake', CURRENT_TIMESTAMP),
    (5, 1, 'RECEIPT', 35, 0, 35, 'PO-2026-001', 'Initial warehouse inventory intake', CURRENT_TIMESTAMP),
    (1, 2, 'RECEIPT', 80, 0, 80, 'PO-2026-002', 'Initial regional stock transfer intake', CURRENT_TIMESTAMP),
    (2, 2, 'RECEIPT', 25, 0, 25, 'PO-2026-002', 'Initial regional stock transfer intake', CURRENT_TIMESTAMP),
    (3, 2, 'RECEIPT', 50, 0, 50, 'PO-2026-002', 'Initial regional stock transfer intake', CURRENT_TIMESTAMP),
    (4, 2, 'RECEIPT', 120, 0, 120, 'PO-2026-002', 'Initial regional stock transfer intake', CURRENT_TIMESTAMP),
    (5, 2, 'RECEIPT', 15, 0, 15, 'PO-2026-002', 'Initial regional stock transfer intake', CURRENT_TIMESTAMP);
