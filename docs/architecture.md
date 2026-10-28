# TradeWay Architecture

## Overview
TradeWay is structured as a modular monolith with a shared backend and two separate user surfaces:

- Customer portal for trading and account management
- Employee portal for monitoring, reporting, and administration

The design keeps operational trading and analytics concerns separate. Transactional data is stored in a primary database, while summary data is synchronized into a dedicated reporting data store.

## Runtime components

### Customer web app
- Frontend: React + TypeScript + Vite
- Responsibilities:
  - user registration and sign-in
  - dashboard summarization
  - market browsing and order entry
  - portfolio and order history review

### Employee web app
- Frontend: React + TypeScript + Vite
- Responsibilities:
  - staff sign-in
  - reporting KPI cards and charts
  - operational health monitoring
  - admin-only account status changes

### Backend service
- Java 21 + Spring Boot 3.4.2
- Responsibilities:
  - authentication and authorization
  - customer account operations
  - order validation and execution
  - ledger and position updates
  - reporting synchronization and admin controls

### Datastores
- Primary PostgreSQL database: operational trading ledger and account state
- Reporting PostgreSQL database: summary metrics and staff dashboard data
- H2 test database: in-memory data for automated validation

## Security model
- Spring Security enforces authenticated access rules.
- Customer and employee flows are separated by route and role checks.
- Admin-only actions are protected behind controller and service-level authorization.
- Suspended accounts cannot authenticate for customer use.

## Trading flow
1. Customer submits a buy or sell order through the customer portal.
2. The backend validates the request against account, cash, and position rules.
3. The trading service creates or updates order state and executes when preconditions are satisfied.
4. Cash, position, and trade ledger records are persisted in the transactional database.
5. Relevant metrics are emitted to the outbox so the reporting pipeline can synchronize summaries.

## Reporting flow
1. Transactional events are written to an outbox table.
2. A reporting sync service reads the outbox.
3. Summary data is inserted into the reporting database.
4. The employee dashboard reads reporting data for charts and KPIs.

## Operational notes
- This is intentionally a simulated market platform rather than a production broker system.
- Market data and balances are educational and synthetic.
- The app is designed to be locally runnable, demonstrable, and testable without exposing real financial data.
