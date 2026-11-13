# Inventra - Multi-Warehouse Inventory Management System

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-green)
![React](https://img.shields.io/badge/React-19-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)

Inventra is a full-stack, multi-warehouse inventory management system designed to handle high-concurrency stock operations with strict transactional integrity. Built with a focus on enterprise architecture, it provides a reliable audit trail for all inventory movements and prevents race conditions during inter-warehouse transfers.

---

## 🚀 Key Features

* **Multi-Warehouse Tracking:** Manage and monitor real-time stock positions across multiple physical warehouses.
* **Audit Trail Ledger:** Every stock mutation (receipt, adjustment, transfer) is recorded in an immutable `stock_movements` ledger table.
* **Concurrency Control:** Combines **Optimistic Locking** (`@Version`) for general updates and **Pessimistic Write Locking** (`PESSIMISTIC_WRITE`) during inter-warehouse transfers to prevent stock corruption.
* **Deadlock Prevention:** Canonical ID ordering during row-lock acquisition prevents database deadlocks when concurrent transfers occur back and forth between two warehouses.
* **Data Integrity:** Strict database constraints (e.g., `CHECK (quantity >= 0)`) and service-layer validations ensure stock levels never drop below zero.
* **Operations Terminal UI:** Financial/terminal-grade dashboard built with React 19 and custom CSS, providing dense data tables with vertical grid lines, real-time filtering, and sub-second feedback.
* **Fully Containerized:** A single Docker Compose command spins up PostgreSQL, the Spring Boot API, and the React frontend with Nginx.

---

## 🧠 Technical Design Decisions

This project was built with banking and fintech principles in mind—where data integrity and concurrency are paramount.

1. **Immutable Ledger:** Stock records represent current state, but every change is recorded as an immutable movement entry. If discrepancies occur, auditors can trace exactly *who* moved *what*, *when*, and under what *reference ID* via `stock_movements`.
2. **Transfer Deadlock Prevention:** When moving stock between Warehouse A and Warehouse B, two separate stock rows are locked. If Thread 1 moves $A \to B$ and Thread 2 moves $B \to A$, acquiring locks in arbitrary order causes a classic database deadlock. Inventra enforces canonical ID ordering (`Math.min` before `Math.max`) so all threads acquire locks in identical sequential order.
3. **Separation of Read/Write Paths:** Read queries and high-contention transactional write operations (`StockTransactionService`) are decoupled to minimize transaction hold times and maximize throughput.
4. **Optimistic Retry Loop:** When multiple users or workers update the same stock row concurrently, optimistic locking (`ObjectOptimisticLockingFailureException`) or constraint violations are caught by retry logic with backoff before falling back to clean client responses.

---

## 🛠️ Tech Stack

### Backend
* **Runtime:** Java 25
* **Framework:** Spring Boot 4.1.2 (Spring Data JPA, Spring Web, Validation)
* **Database & ORM:** PostgreSQL 16, Hibernate 7
* **Testing:** JUnit 5, Mockito, Spring Boot Test, H2 Database (In-Memory Postgres Mode)
* **Build Tool:** Maven

### Frontend
* **Runtime / Bundler:** Node.js, Vite 8
* **Library:** React 19
* **HTTP Client:** Axios (configured with Vite reverse-proxy and environment fallback)
* **Styling:** Custom "Operations Terminal" Design System (IBM Plex Sans & IBM Plex Mono typography, high-contrast financial palette)

### Infrastructure
* **Containerization:** Docker & Docker Compose
* **Web Server:** Nginx (Alpine-based frontend container reverse-proxying `/api` to the backend)

---

## 🚦 Getting Started

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.
* Verify ports `3000` (Frontend), `8080` (Backend API), and `5432` (PostgreSQL) are available.

### Quick Start (Docker Compose)

Run the entire stack with a single command from the project root:

```bash
docker compose up -d --build
```

#### Service URLs
* **Frontend UI:** [http://localhost:3000](http://localhost:3000)
* **Backend API:** [http://localhost:8080/api](http://localhost:8080/api)
* **Health Check:** [http://localhost:8080/api/health](http://localhost:8080/api/health)
* **PostgreSQL:** `localhost:5432` (Database: `inventra`, User: `inventra_user`, Password: `inventra_pass`)

To stop all services:
```bash
docker compose down
```

---

## 💻 Local Development Setup (Without Docker)

### 1. Database Setup
Start a PostgreSQL 16 instance, or spin up only the database via Docker:
```bash
docker compose up -d db
```
The database initialization scripts in `inventra-backend/src/main/resources/schema.sql` and `data.sql` will execute automatically upon first initialization.

### 2. Backend Setup
From the `inventra-backend/` directory:
```bash
cd inventra-backend

# Run test suite (runs in-memory via H2 PostgreSQL-compatibility mode)
./mvnw test

# Start the Spring Boot application
./mvnw spring-boot:run
```
The backend starts on `http://localhost:8080`.

### 3. Frontend Setup
From the `inventra-frontend/` directory:
```bash
cd inventra-frontend

# Install dependencies
npm install

# Start Vite development server (proxies /api requests to http://localhost:8080)
npm run dev
```
The frontend dev server starts on `http://localhost:5173`.

---

## 📡 API Reference

All backend endpoints are prefixed with `/api`.

### Health
* `GET /api/health` — Returns system health status and timestamp.

### Warehouses (`/api/warehouses`)
* `GET /api/warehouses` — List all registered warehouses.
* `GET /api/warehouses/{id}` — Fetch details for a specific warehouse.
* `POST /api/warehouses` — Create a warehouse (`name`, `code`, `address`, `capacity`).
* `DELETE /api/warehouses/{id}` — Delete warehouse by ID.

### Products (`/api/products`)
* `GET /api/products` — List all catalog products.
* `GET /api/products/{id}` — Fetch details for a specific product.
* `POST /api/products` — Create a product (`sku`, `name`, `description`, `unitCost`, `reorderPoint`).
* `DELETE /api/products/{id}` — Delete product by ID.

### Stock & Inventory (`/api/stock`)
* `GET /api/stock` — Retrieve all current stock positions across warehouses.
* `GET /api/stock?warehouseId={id}` — Filter stock positions by warehouse ID.
* `GET /api/stock/{productId}/{warehouseId}` — Fetch specific product position in a warehouse.

### Transactional Operations
* `POST /api/stock/receipt` — Inbound stock receipt:
  ```json
  {
    "productId": 1,
    "warehouseId": 1,
    "quantity": 100,
    "referenceId": "PO-98214",
    "notes": "Initial batch"
  }
  ```
* `POST /api/stock/adjustment` — Audit adjustment (sets exact quantity, logs signed delta):
  ```json
  {
    "productId": 1,
    "warehouseId": 1,
    "newQuantity": 95,
    "reason": "Cycle count reconciliation",
    "notes": "Damaged goods written off"
  }
  ```
* `POST /api/stock/transfer` — Atomic inter-warehouse transfer with canonical lock ordering:
  ```json
  {
    "productId": 1,
    "sourceWarehouseId": 1,
    "targetWarehouseId": 2,
    "quantity": 25,
    "referenceId": "TR-4401",
    "notes": "Stock rebalancing"
  }
  ```

---

## 📂 Project Structure

```
Inventra/
├── docker-compose.yml           # Multi-container orchestration (DB, API, Web)
├── .env.example                 # Environment configuration template
├── README.md                    # Project documentation
│
├── inventra-backend/            # Spring Boot 4 REST API
│   ├── pom.xml                  # Maven dependencies & build plugins
│   ├── Dockerfile               # Multi-stage container build
│   └── src/
│       ├── main/
│       │   ├── java/com/inventra/
│       │   │   ├── config/      # CORS and MVC configurations
│       │   │   ├── exception/   # Global exception handling & custom errors
│       │   │   ├── health/      # Health check endpoints
│       │   │   ├── product/     # Product catalog entities, repositories & controllers
│       │   │   ├── stock/       # Stock tracking, ledger, & deadlock-free transactions
│       │   │   └── warehouse/   # Warehouse management
│       │   └── resources/
│       │       ├── application.yml   # Production / local profile config
│       │       ├── schema.sql        # DDL table schemas & constraints
│       │       └── data.sql          # Seed data
│       └── test/
│           ├── java/com/inventra/    # Controller, service & concurrency unit tests
│           └── resources/
│               └── application-test.yml # In-memory H2 PostgreSQL mode profile
│
└── inventra-frontend/           # React 19 Frontend
    ├── package.json             # NPM dependencies & scripts
    ├── vite.config.js           # Vite dev proxy configuration
    ├── Dockerfile               # Production Nginx container build
    ├── nginx.conf               # Nginx reverse proxy configuration
    └── src/
        ├── api/                 # Axios API client
        ├── components/          # Stock tables, operations forms
        ├── pages/               # Warehouses, Products, & Stock views
        ├── App.css              # Operations Terminal design tokens & styling
        └── App.jsx              # Navigation and main layout
```

---

## 🧪 Testing

The test suite runs with an isolated in-memory H2 database using PostgreSQL compatibility mode (`@ActiveProfiles("test")`), allowing all tests to execute without an external database instance:

```bash
cd inventra-backend
./mvnw test
```

Frontend production bundle verification:
```bash
cd inventra-frontend
npm run build
```

---

## 📄 License
This project is licensed under the MIT License.
