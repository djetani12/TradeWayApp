# TradeWay Progress Log

## Milestone 1
Status: Complete for the foundation scaffold

Completed and verified:
- Repository inspected and existing work preserved.
- Toolchain validated for the required Node 22 and Java 21 stack.
- Architecture requirements documented.
- GitHub Copilot project instructions created.
- Initial project structure created for customer, employee, and backend apps.
- Docker Compose and environment files prepared.
- README updated with local startup instructions.
- Initial backend health endpoint scaffold created.
- Frontend and backend builds verified successfully with the supported runtime versions.

Open items before milestone 2 starts:
- Add Flyway migrations for the transactional schema.
- Implement authentication and account management.
- Add business-domain entities, services, and order validation.
- Extend the backend from scaffold to real customer and employee workflows.
- Add additional operational configuration and seed data for demo accounts and instruments.

## Milestone 2 and 3
Status: Complete for backend auth and simulation engine

Completed and verified:
- Spring Security, session-based auth, and CSRF handling are active.
- Demo customer and admin users are seeded locally with production-like credentials.
- Customer accounts, ledger entries, and trading instruments are persisted with Flyway.
- Order lifecycle, execution, cancellations, and portfolio logic are implemented and tested.
- Trade validation covers buy, sell, insufficient holdings, and pending-order scenarios.

## Milestone 4
Status: Complete for the customer-facing web portal

Completed and verified:
- Customer portal login and registration flows are implemented with live backend APIs.
- Portfolio dashboard displays cash, buying power, value, positions, and diversification.
- Market catalog and watchlist use live quote data from the backend.
- Trading ticket supports buy and sell flows with order validation and realtime refresh.
- Pending orders and recent order history are surfaced in the customer view.
- Beginner trading tutorial, account settings, and responsive dashboard shell are included.
- Customer web app compiles successfully in production build mode.

## Milestone 5
Status: Complete for employee operations and reporting

Completed and verified:
- Employee portal is implemented as a separate React app on port 5174.
- Employee login is protected by the same secure session and CSRF-based auth model as the customer apps.
- Admin and analyst authorization is enforced by backend security rules and controller-level checks.
- Reporting dashboard provides KPI cards, activity charts, order status summaries, and operational health details.
- A dedicated reporting datasource and outbox-based synchronization pattern are present to keep analytics separate from the transactional store.
- Administrative actions for suspending and reactivating accounts are guarded for admin-only access.
- Backend tests cover reporting summary logic and permission enforcement.

## Milestone 6
Status: Complete for integration stabilization and demo readiness

Completed and verified:
- Final project documentation has been aligned with the current implementation and runtime requirements.
- Node 22 is enforced as the supported frontend runtime to avoid Vite 8 / Rolldown incompatibility on Node 20.
- Backend validation passes with Java 21 and the Spring Boot test suite.
- Customer and employee frontends build successfully under Node 22.
- Demo and architecture guidance is documented for local setup, feature validation, and stakeholder walkthroughs.

How to validate locally:
1. Start the backend from apps/backend with Java 21 and run the application on port 8080.
2. Use Node 22 via `nvm use 22` or `npx -p node@22` before starting either frontend app.
3. Start the frontend from apps/client-web with `npm install` and then `npm run dev -- --host 0.0.0.0`.
4. Start the employee app from apps/employee-web with `npm install` and then `npm run dev -- --host 0.0.0.0`.
5. Open the customer portal at http://localhost:5173 and the employee portal at http://localhost:5174.
6. Sign in with the demo track as `demo` for the customer portal and `admin` for employee controls.
7. Review the employee dashboard and system health rollups, then verify the admin-only user suspension flow is protected.

Current verification evidence:
- `apps/backend`: the Maven test suite passed on Java 21 with exit code 0.
- `apps/client-web`: production bundle built successfully with Node 22 (`npx -p node@22 node ./node_modules/vite/bin/vite.js build`).
- `apps/employee-web`: production bundle built successfully with Node 22 (`npx -p node@22 node ./node_modules/vite/bin/vite.js build`).
- The project now documents the correct runtime requirement to avoid the known Node 20 incompatibility encountered with Vite 8 and Rolldown.
