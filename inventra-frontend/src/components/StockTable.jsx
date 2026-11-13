import React from 'react';

/**
 * Shared Stock Table component.
 * Rows where quantity is below minStockLevel are highlighted in red.
 */
export default function StockTable({ stocks }) {
  if (!stocks || stocks.length === 0) {
    return <div className="table-empty">— NO STOCK RECORDS —</div>;
  }

  const getStatus = (item) => {
    if (item.quantity === 0 || item.availableQuantity <= 0) {
      return { label: 'OUT', className: 'status-badge status-void' };
    }
    if (item.quantity < item.minStockLevel) {
      return { label: 'LOW', className: 'status-badge status-low' };
    }
    return { label: 'IN STOCK', className: 'status-badge status-ok' };
  };

  return (
    <div className="table-responsive">
      <table className="data-table">
        <thead>
          <tr>
            <th>SKU</th>
            <th>PRODUCT</th>
            <th style={{ textAlign: 'right' }}>QTY</th>
            <th style={{ textAlign: 'right' }}>RESERVED</th>
            <th style={{ textAlign: 'right' }}>AVAILABLE</th>
            <th>STATUS</th>
          </tr>
        </thead>
        <tbody>
          {stocks.map((item) => {
            const isBelowMin = item.quantity < item.minStockLevel;
            const status = getStatus(item);
            return (
              <tr
                key={`${item.productId}-${item.warehouseId}`}
                className={isBelowMin ? 'row-critical-stock' : ''}
              >
                <td className="code-cell">{item.productSku}</td>
                <td>
                  {item.productName}
                  <span className="cell-sub">{item.warehouseCode}</span>
                </td>
                <td className="num-cell">{item.quantity}</td>
                <td className="num-cell">{item.reservedQuantity}</td>
                <td className="num-cell">{item.availableQuantity}</td>
                <td>
                  <span className={status.className}>{status.label}</span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
