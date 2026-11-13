import React, { useState, useEffect, useCallback } from 'react';
import api, { getErrorMessage } from '../api/api';
import StockTable from '../components/StockTable';
import StockOperationForm from '../components/StockOperationForm';

export default function StockPage() {
  const [stocks, setStocks] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load lookup lists (warehouses and products) once
  useEffect(() => {
    const fetchMetadata = async () => {
      try {
        const [whRes, prodRes] = await Promise.all([
          api.get('/warehouses'),
          api.get('/products'),
        ]);
        setWarehouses(whRes.data);
        setProducts(prodRes.data);
      } catch (err) {
        setError(getErrorMessage(err));
      }
    };
    fetchMetadata();
  }, []);

  // Fetch stocks filtered by selected warehouse
  const fetchStock = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const url = selectedWarehouseId
        ? `/stock?warehouseId=${selectedWarehouseId}`
        : '/stock';
      const res = await api.get(url);
      setStocks(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [selectedWarehouseId]);

  useEffect(() => {
    fetchStock();
  }, [fetchStock]);

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h2>Stock Snapshot</h2>
          <p className="page-subtitle">Real-time inventory levels, reservations, and critical alerts</p>
        </div>
        <div className="filter-group">
          <label htmlFor="filter-warehouse">LOCATION:</label>
          <select
            id="filter-warehouse"
            value={selectedWarehouseId}
            onChange={(e) => setSelectedWarehouseId(e.target.value)}
            className="filter-select"
          >
            <option value="">ALL WAREHOUSES</option>
            {warehouses.map((w) => (
              <option key={w.id} value={w.id}>
                {w.code} ({w.name})
              </option>
            ))}
          </select>
          <button className="btn btn-secondary btn-sm" onClick={fetchStock} title="Refresh Table">
            REFRESH
          </button>
        </div>
      </div>

      {error && (
        <div className="inline-feedback error" role="alert">
          ERROR: {error}
        </div>
      )}

      {loading ? (
        <div className="loading-text">QUERYING STOCK POSITIONS...</div>
      ) : (
        <StockTable stocks={stocks} />
      )}

      <div className="stock-operations-section">
        <StockOperationForm
          warehouses={warehouses}
          products={products}
          onOperationSuccess={fetchStock}
        />
      </div>
    </div>
  );
}
