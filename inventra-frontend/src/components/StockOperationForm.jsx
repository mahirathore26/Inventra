import React, { useState } from 'react';
import api, { getErrorMessage } from '../api/api';

export default function StockOperationForm({ warehouses, products, onOperationSuccess }) {
  const [activeTab, setActiveTab] = useState('RECEIPT'); // RECEIPT, ADJUSTMENT, TRANSFER
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Form State
  const [productId, setProductId] = useState('');
  const [warehouseId, setWarehouseId] = useState('');
  const [sourceWarehouseId, setSourceWarehouseId] = useState('');
  const [targetWarehouseId, setTargetWarehouseId] = useState('');
  const [quantity, setQuantity] = useState('');
  const [newQuantity, setNewQuantity] = useState('');
  const [referenceId, setReferenceId] = useState('');
  const [reason, setReason] = useState('');
  const [notes, setNotes] = useState('');

  const resetMessages = () => {
    setError(null);
    setSuccessMessage(null);
  };

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    resetMessages();
  };

  const handleReceipt = async (e) => {
    e.preventDefault();
    resetMessages();
    setLoading(true);
    try {
      const payload = {
        productId: Number(productId),
        warehouseId: Number(warehouseId),
        quantity: Number(quantity),
        referenceId: referenceId || undefined,
        notes: notes || undefined,
      };
      const res = await api.post('/stock/receipt', payload);
      setSuccessMessage(`Receipt recorded: ${res.data.productSku} quantity is now ${res.data.quantity}.`);
      setQuantity('');
      setReferenceId('');
      setNotes('');
      if (onOperationSuccess) onOperationSuccess();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleAdjustment = async (e) => {
    e.preventDefault();
    resetMessages();
    setLoading(true);
    try {
      const payload = {
        productId: Number(productId),
        warehouseId: Number(warehouseId),
        newQuantity: Number(newQuantity),
        reason: reason,
      };
      const res = await api.post('/stock/adjustment', payload);
      setSuccessMessage(`Stock adjusted: ${res.data.productSku} quantity is now ${res.data.quantity}.`);
      setNewQuantity('');
      setReason('');
      if (onOperationSuccess) onOperationSuccess();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleTransfer = async (e) => {
    e.preventDefault();
    resetMessages();

    if (sourceWarehouseId === targetWarehouseId) {
      setError('Source and target warehouses cannot be identical.');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        productId: Number(productId),
        sourceWarehouseId: Number(sourceWarehouseId),
        targetWarehouseId: Number(targetWarehouseId),
        quantity: Number(quantity),
        referenceId: referenceId || undefined,
        notes: notes || undefined,
      };
      const res = await api.post('/stock/transfer', payload);
      setSuccessMessage(
        `Transfer successful (${res.data.referenceId || 'Completed'}). Source Qty: ${res.data.sourceStock.quantity}, Target Qty: ${res.data.targetStock.quantity}.`
      );
      setQuantity('');
      setReferenceId('');
      setNotes('');
      if (onOperationSuccess) onOperationSuccess();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="operation-container">
      <div className="operation-header">
        <h3 className="operation-title">Stock Operations</h3>
        <div className="tab-group">
          <button
            type="button"
            className={`tab-btn ${activeTab === 'RECEIPT' ? 'active' : ''}`}
            onClick={() => handleTabChange('RECEIPT')}
          >
            RECEIPT
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'ADJUSTMENT' ? 'active' : ''}`}
            onClick={() => handleTabChange('ADJUSTMENT')}
          >
            ADJUSTMENT
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'TRANSFER' ? 'active' : ''}`}
            onClick={() => handleTabChange('TRANSFER')}
          >
            TRANSFER
          </button>
        </div>
      </div>

      <div className="operation-body">
        {activeTab === 'RECEIPT' && (
          <form onSubmit={handleReceipt} className="form-grid">
            <div className="form-group">
              <label htmlFor="receipt-product">PRODUCT *</label>
              <select
                id="receipt-product"
                required
                value={productId}
                onChange={(e) => setProductId(e.target.value)}
              >
                <option value="">SELECT PRODUCT</option>
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.sku} — {p.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="receipt-warehouse">DESTINATION WAREHOUSE *</label>
              <select
                id="receipt-warehouse"
                required
                value={warehouseId}
                onChange={(e) => setWarehouseId(e.target.value)}
              >
                <option value="">SELECT WAREHOUSE</option>
                {warehouses.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.code} ({w.name})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="receipt-qty">QUANTITY *</label>
              <input
                id="receipt-qty"
                type="number"
                min="1"
                required
                className="font-mono"
                placeholder="e.g. 50"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="receipt-ref">REFERENCE (PO / INVOICE)</label>
              <input
                id="receipt-ref"
                type="text"
                placeholder="e.g. PO-2026-001"
                value={referenceId}
                onChange={(e) => setReferenceId(e.target.value)}
              />
            </div>

            <div className="form-group full-width">
              <label htmlFor="receipt-notes">NOTES</label>
              <input
                id="receipt-notes"
                type="text"
                placeholder="Supplier shipment details..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
              />
            </div>

            <div className="form-actions full-width">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'PROCESSING...' : 'PROCESS RECEIPT'}
              </button>
            </div>
          </form>
        )}

        {activeTab === 'ADJUSTMENT' && (
          <form onSubmit={handleAdjustment} className="form-grid">
            <div className="form-group">
              <label htmlFor="adj-product">PRODUCT *</label>
              <select
                id="adj-product"
                required
                value={productId}
                onChange={(e) => setProductId(e.target.value)}
              >
                <option value="">SELECT PRODUCT</option>
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.sku} — {p.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="adj-warehouse">WAREHOUSE *</label>
              <select
                id="adj-warehouse"
                required
                value={warehouseId}
                onChange={(e) => setWarehouseId(e.target.value)}
              >
                <option value="">SELECT WAREHOUSE</option>
                {warehouses.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.code} ({w.name})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="adj-new-qty">NEW QUANTITY *</label>
              <input
                id="adj-new-qty"
                type="number"
                min="0"
                required
                className="font-mono"
                placeholder="e.g. 120"
                value={newQuantity}
                onChange={(e) => setNewQuantity(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="adj-reason">AUDIT REASON *</label>
              <input
                id="adj-reason"
                type="text"
                required
                placeholder="e.g. Annual physical count audit"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
              />
            </div>

            <div className="form-actions full-width">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'APPLYING...' : 'APPLY ADJUSTMENT'}
              </button>
            </div>
          </form>
        )}

        {activeTab === 'TRANSFER' && (
          <form onSubmit={handleTransfer} className="form-grid">
            <div className="form-group full-width">
              <label htmlFor="xfer-product">PRODUCT *</label>
              <select
                id="xfer-product"
                required
                value={productId}
                onChange={(e) => setProductId(e.target.value)}
              >
                <option value="">SELECT PRODUCT</option>
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.sku} — {p.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="xfer-source">SOURCE WAREHOUSE *</label>
              <select
                id="xfer-source"
                required
                value={sourceWarehouseId}
                onChange={(e) => setSourceWarehouseId(e.target.value)}
              >
                <option value="">SELECT SOURCE</option>
                {warehouses.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.code} ({w.name})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="xfer-target">TARGET WAREHOUSE *</label>
              <select
                id="xfer-target"
                required
                value={targetWarehouseId}
                onChange={(e) => setTargetWarehouseId(e.target.value)}
              >
                <option value="">SELECT TARGET</option>
                {warehouses.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.code} ({w.name})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="xfer-qty">QUANTITY *</label>
              <input
                id="xfer-qty"
                type="number"
                min="1"
                required
                className="font-mono"
                placeholder="e.g. 25"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="xfer-ref">REFERENCE ID</label>
              <input
                id="xfer-ref"
                type="text"
                placeholder="e.g. TR-2026-001"
                value={referenceId}
                onChange={(e) => setReferenceId(e.target.value)}
              />
            </div>

            <div className="form-actions full-width">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'EXECUTING...' : 'EXECUTE TRANSFER'}
              </button>
            </div>
          </form>
        )}

        {error && (
          <div className="inline-feedback error" role="alert">
            ERROR: {error}
          </div>
        )}

        {successMessage && (
          <div className="inline-feedback success" role="alert">
            SUCCESS: {successMessage}
          </div>
        )}
      </div>
    </div>
  );
}
