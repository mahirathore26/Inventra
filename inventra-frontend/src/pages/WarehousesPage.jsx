import React, { useState, useEffect } from 'react';
import api, { getErrorMessage } from '../api/api';

export default function WarehousesPage() {
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // New warehouse form state
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [address, setAddress] = useState('');
  const [capacity, setCapacity] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState(null);

  const fetchWarehouses = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/warehouses');
      setWarehouses(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWarehouses();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setActionError(null);
    setActionLoading(true);
    try {
      await api.post('/warehouses', {
        name,
        code,
        address,
        capacity: Number(capacity),
      });
      setName('');
      setCode('');
      setAddress('');
      setCapacity('');
      setShowForm(false);
      fetchWarehouses();
    } catch (err) {
      setActionError(getErrorMessage(err));
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (id, codeName) => {
    if (!window.confirm(`Are you sure you want to delete warehouse ${codeName}?`)) {
      return;
    }
    setError(null);
    try {
      await api.delete(`/warehouses/${id}`);
      fetchWarehouses();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h2>Warehouses</h2>
          <p className="page-subtitle">Directory of storage facilities and capacity parameters</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => {
            setShowForm(!showForm);
            setActionError(null);
          }}
        >
          {showForm ? 'Cancel' : '+ Add Warehouse'}
        </button>
      </div>

      {error && (
        <div className="inline-feedback error" role="alert">
          ERROR: {error}
        </div>
      )}

      {showForm && (
        <div className="inline-form-container">
          <div className="inline-form-title">REGISTER WAREHOUSE</div>
          {actionError && (
            <div className="inline-feedback error" role="alert">
              ERROR: {actionError}
            </div>
          )}
          <form onSubmit={handleCreate} className="form-grid">
            <div className="form-group">
              <label htmlFor="wh-name">NAME *</label>
              <input
                id="wh-name"
                type="text"
                required
                placeholder="e.g. Midwest Logistics Hub"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="wh-code">CODE *</label>
              <input
                id="wh-code"
                type="text"
                required
                className="font-mono"
                placeholder="e.g. WH-MIDWEST-01"
                value={code}
                onChange={(e) => setCode(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="wh-capacity">CAPACITY (UNITS) *</label>
              <input
                id="wh-capacity"
                type="number"
                min="1"
                required
                className="font-mono"
                placeholder="e.g. 50000"
                value={capacity}
                onChange={(e) => setCapacity(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="wh-address">ADDRESS *</label>
              <input
                id="wh-address"
                type="text"
                required
                placeholder="e.g. 700 Freightway Blvd, Columbus, OH"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                {actionLoading ? 'SAVING...' : 'SAVE WAREHOUSE'}
              </button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="loading-text">LOADING WAREHOUSES...</div>
      ) : warehouses.length === 0 ? (
        <div className="table-empty">— NO WAREHOUSES REGISTERED —</div>
      ) : (
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>NAME</th>
                <th>CODE</th>
                <th>ADDRESS</th>
                <th>CAPACITY</th>
                <th>CREATED</th>
                <th>ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {warehouses.map((wh) => (
                <tr key={wh.id}>
                  <td className="num-cell">{wh.id}</td>
                  <td>{wh.name}</td>
                  <td className="code-cell">{wh.code}</td>
                  <td>{wh.address}</td>
                  <td className="num-cell">{wh.capacity.toLocaleString()}</td>
                  <td className="num-cell">{new Date(wh.createdAt).toISOString().split('T')[0]}</td>
                  <td>
                    <button
                      className="btn-delete"
                      onClick={() => handleDelete(wh.id, wh.code)}
                      title="Delete warehouse"
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
