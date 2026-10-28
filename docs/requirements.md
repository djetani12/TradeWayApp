# TradeWay Requirements

## Purpose
TradeWay is a simulated stock trading platform designed for education and product demonstration. It provides a customer experience for account management, market participation, and portfolio tracking, and a separate employee experience for supervision, operational reporting, and account governance.

## Functional requirements

### Customer-facing experience
1. Customers must be able to create an account and sign in securely.
2. Authenticated customers must see a dashboard with cash, buying power, positions, and recent activity.
3. Customers must be able to browse market instruments and request simulated buy/sell trades.
4. Trade requests must validate account status, available balance, and position constraints.
5. Orders must enter a controlled lifecycle with pending, executed, cancelled, and rejected states.
6. Executed trades must update holdings, cash balance, and trade history in a consistent manner.
7. Customers must be able to review pending and recent orders without exposing internal staff-only controls.
8. The platform must clearly label all market and portfolio data as educational simulation data.

### Employee and admin experience
1. The employee portal must be separated from the customer portal and protected by role-aware access rules.
2. Analysts can access reporting and operational summaries, but cannot modify account state.
3. Admins can suspend or reactivate accounts when required by operational policy.
4. Staff dashboards must display operational KPIs, order flow, and health information.
5. Reporting data must be isolated from transactional operational storage to reduce coupling and support analytics workloads.

### Data and platform requirements
1. Transactional trading data must be persisted in a primary relational datastore.
2. Reporting and analytics views must be generated from a separate reporting datastore.
3. The system should use durable outbox-style synchronization for metrics and summaries.
4. The local environment should support a repeatable developer workflow with Dockerized PostgreSQL services.
5. The application should work on a supported Java 21 and Node 22 toolchain.

## Non-functional requirements
- Security: secure session handling, auth checks, and role boundaries for customer, analyst, and admin roles.
- Observability: health endpoints and operational dashboards for backend and reporting status.
- Reliability: consistent ledger and position updates for simulated execution flows.
- Portability: support for local developer runbooks with minimal friction and clear runtime requirements.
- Transparency: simulation-only scope; no claims of production-grade investment operations or personal financial data.

## Requirement traceability
- Registration and signing in: implemented in the customer auth flow and backend security configuration.
- Trading execution and ledger updates: covered by the trading service integration tests and account/position services.
- Suspensions and admin-only actions: enforced in the employee controller and security tests.
- Reporting and dashboarding: implemented through the reporting datasource, reporting service, and employee UI.
- Toolchain enforcement: documented and validated with Node 22 and Java 21.
