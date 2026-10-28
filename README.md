# TradeWay

TradeWay is a simulated stock and bond trading platform for educational use. The project includes a customer web app, an employee web app, and a shared Java backend service.

## Stack
- Frontend: React + TypeScript + Vite + Tailwind-ready structure
- Employee frontend: React + TypeScript + Vite
- Backend: Java 21 + Spring Boot 3.4.2 + Spring Security + Spring Data JPA + Maven
- Databases: PostgreSQL primary and reporting databases via Docker Compose
- Tooling: Flyway, JUnit 5, and production front-end builds verified on the supported runtime stack

## Prerequisites
- Node 22.x (required; Vite 8 / Rolldown fails on Node 20)
- Java 21
- Maven 3.10+
- Docker Desktop or Docker Engine with Compose support
- Git

## Local setup
1. Use the supported Node version before installing dependencies:
   ```bash
   nvm install 22
   nvm use 22
   ```
   If you do not use nvm, you can also run one-off commands with `npx -p node@22`.
2. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
3. Add or adjust the environment values as needed for your local setup.
4. Start PostgreSQL databases:
   ```bash
   docker compose up -d
   ```
5. Install frontend dependencies:
   ```bash
   cd apps/client-web && npm install
   cd ../employee-web && npm install
   ```
6. Start the frontend apps:
   ```bash
   cd apps/client-web && npm run dev -- --host 0.0.0.0
   cd ../employee-web && npm run dev -- --host 0.0.0.0
   ```
7. Start the backend:
   ```bash
   cd apps/backend
   export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
   export PATH="$JAVA_HOME/bin:$PATH"
   mvn spring-boot:run
   ```

> If the terminal reports a `node:util` export error during `vite build`, the machine is running Node 20 or older. Switch to Node 22 before continuing.

## Default local ports
- Customer web: http://localhost:5173
- Employee web: http://localhost:5174
- Backend: http://localhost:8080
- Primary PostgreSQL: localhost:5432
- Reporting PostgreSQL: localhost:5433

## Health checks
- Backend health: http://localhost:8080/actuator/health
- Custom API health: http://localhost:8080/api/health

## Useful commands
```bash
# Use the supported runtime implicitly with nvm
nvm use 22

# Build frontend apps with the supported runtime
cd apps/client-web && npx -p node@22 node ./node_modules/vite/bin/vite.js build
cd ../employee-web && npx -p node@22 node ./node_modules/vite/bin/vite.js build

# Build backend
cd apps/backend && export JAVA_HOME="$(/usr/libexec/java_home -v 21)" && export PATH="$JAVA_HOME/bin:$PATH" && mvn test
cd apps/backend && export JAVA_HOME="$(/usr/libexec/java_home -v 21)" && export PATH="$JAVA_HOME/bin:$PATH" && mvn package

# Reset local DB data
docker compose down -v
```

## Notes
- The customer trading engine and employee reporting layer are implemented as separate operational surfaces, with employee access restricted to admin/analyst roles.
- Docker is required for the primary and reporting databases, but it may not be available in every local environment.
- Market data remains simulated and clearly labeled as educational demo data.
- Analytics queries and reporting consumers are isolated from the transactional trading database so operational reporting is not performed directly against the live ledger.

## Milestone 5
- Separate employee portal available on http://localhost:5174.
- Secure admin and analyst sign-in is available using the seeded admin credentials.
- Reporting dashboard includes KPI cards, period summaries, status rollups, and platform health information.
- Reporting uses a dedicated reporting datasource and transactional outbox pattern for synchronization.
- Administrative actions include account suspension and reactivation for staff members with admin privileges.
