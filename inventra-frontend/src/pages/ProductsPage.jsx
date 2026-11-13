import React, { useState, useEffect } from 'react';
import api, { getErrorMessage } from '../api/api';

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // New product form state
  const [showForm, setShowForm] = useState(false);
  const [sku, setSku] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [unitCost, setUnitCost] = useState('');
  const [reorderPoint, setReorderPoint] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState(null);

  const fetchProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/products');
      setProducts(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setActionError(null);
    setActionLoading(true);
    try {
      await api.post('/products', {
        sku,
        name,
        description: description || undefined,
        unitCost: Number(unitCost),
        reorderPoint: Number(reorderPoint),
      });
      setSku('');
      setName('');
      setDescription('');
      setUnitCost('');
      setReorderPoint('');
      setShowForm(false);
      fetchProducts();
    } catch (err) {
      setActionError(getErrorMessage(err));
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (id, productSku) => {
    if (!window.confirm(`Are you sure you want to delete product ${productSku}?`)) {
      return;
    }
    setError(null);
    try {
      await api.delete(`/products/${id}`);
      fetchProducts();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h2>Products Catalog</h2>
          <p className="page-subtitle">Master product directory, unit costs, and reorder levels</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => {
            setShowForm(!showForm);
            setActionError(null);
          }}
        >
          {showForm ? 'Cancel' : '+ Add Product'}
        </button>
      </div>

      {error && (
        <div className="inline-feedback error" role="alert">
          ERROR: {error}
        </div>
      )}

      {showForm && (
        <div className="inline-form-container">
          <div className="inline-form-title">REGISTER PRODUCT</div>
          {actionError && (
            <div className="inline-feedback error" role="alert">
              ERROR: {actionError}
            </div>
          )}
          <form onSubmit={handleCreate} className="form-grid">
            <div className="form-group">
              <label htmlFor="prod-sku">SKU *</label>
              <input
                id="prod-sku"
                type="text"
                required
                className="font-mono"
                placeholder="e.g. PROD-SKU-007"
                value={sku}
                onChange={(e) => setSku(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="prod-name">NAME *</label>
              <input
                id="prod-name"
                type="text"
                required
                placeholder="e.g. Mechanical Gaming Mouse"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="prod-cost">UNIT COST ($) *</label>
              <input
                id="prod-cost"
                type="number"
                step="0.01"
                min="0"
                required
                className="font-mono"
                placeholder="e.g. 29.99"
                value={unitCost}
                onChange={(e) => setUnitCost(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="prod-reorder">REORDER POINT *</label>
              <input
                id="prod-reorder"
                type="number"
                min="0"
                required
                className="font-mono"
                placeholder="e.g. 15"
                value={reorderPoint}
                onChange={(e) => setReorderPoint(e.target.value)}
              />
            </div>
            <div className="form-group full-width">
              <label htmlFor="prod-desc">DESCRIPTION</label>
              <input
                id="prod-desc"
                type="text"
                placeholder="Key specifications, packaging details..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>
            <div className="form-actions full-width">
              <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                {actionLoading ? 'SAVING...' : 'SAVE PRODUCT'}
              </button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="loading-text">LOADING PRODUCTS...</div>
      ) : products.length === 0 ? (
        <div className="table-empty">— NO PRODUCTS REGISTERED —</div>
      ) : (
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>SKU</th>
                <th>NAME</th>
                <th>DESCRIPTION</th>
                <th style={{ textAlign: 'right' }}>UNIT COST</th>
                <th style={{ textAlign: 'right' }}>REORDER POINT</th>
                <th>ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td className="num-cell">{p.id}</td>
                  <td className="code-cell">{p.sku}</td>
                  <td>{p.name}</td>
                  <td>{p.description || '—'}</td>
                  <td className="num-cell">${Number(p.unitCost).toFixed(2)}</td>
                  <td className="num-cell">{p.reorderPoint}</td>
                  <td>
                    <button
                      className="btn-delete"
                      onClick={() => handleDelete(p.id, p.sku)}
                      title="Delete product"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
