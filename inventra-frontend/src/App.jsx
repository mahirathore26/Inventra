import React, { useState } from 'react';
import WarehousesPage from './pages/WarehousesPage';
import ProductsPage from './pages/ProductsPage';
import StockPage from './pages/StockPage';
import './App.css';

export default function App() {
  const [currentScreen, setCurrentScreen] = useState('stock'); // 'stock', 'warehouses', 'products'

  return (
    <div className="app-layout">
      <header className="app-header">
        <div className="header-brand">
          <h1 className="brand-title">INVENTRA</h1>
        </div>
        <nav className="header-nav">
          <button
            type="button"
            className={`nav-link ${currentScreen === 'warehouses' ? 'active' : ''}`}
            onClick={() => setCurrentScreen('warehouses')}
          >
            WAREHOUSES
          </button>
          <button
            type="button"
            className={`nav-link ${currentScreen === 'products' ? 'active' : ''}`}
            onClick={() => setCurrentScreen('products')}
          >
            PRODUCTS
          </button>
          <button
            type="button"
            className={`nav-link ${currentScreen === 'stock' ? 'active' : ''}`}
            onClick={() => setCurrentScreen('stock')}
          >
            STOCK
          </button>
        </nav>
      </header>

      <main className="app-content">
        {currentScreen === 'stock' && <StockPage />}
        {currentScreen === 'warehouses' && <WarehousesPage />}
        {currentScreen === 'products' && <ProductsPage />}
      </main>

      <footer className="app-footer">
        INVENTRA OPERATIONAL TERMINAL &bull; POSTGRESQL 16 &bull; SPRING BOOT 4.1.2
      </footer>
    </div>
  );
}
