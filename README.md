# TradeWay

TradeWay is a simulated stock and bond trading platform for educational use. The project includes a customer web app, an employee web app, and a shared Java backend service.

## Stack
- Frontend: React + TypeScript + Vite + Tailwind-ready structure
- Employee frontend: React + TypeScript + Vite
- Backend: Java 21 + Spring Boot 3.4.2 + Spring Security + Spring Data JPA + Maven
- Databases: PostgreSQL primary and reporting databases via Docker Compose
- Tooling: Flyway, OpenAPI/Swagger, JUnit 5, Vitest, Playwright

## Prerequisites
- Node 22.x
- Java 21
- Maven 3.10+
- Docker Desktop or Docker Engine with Compose support
- Git

## Local setup
1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
2. Add or adjust the environment values as needed for your local setup.
3. Start PostgreSQL databases:
   ```bash
   docker compose up -d
   ```
4. Install frontend dependencies:
   ```bash
   cd apps/client-web && npm install
   cd ../employee-web && npm install
   ```
5. Start the frontend apps:
   ```bash
   cd apps/client-web && npm run dev -- --host 0.0.0.0
   cd ../employee-web && npm run dev -- --host 0.0.0.0
   ```
6. Start the backend:
   ```bash
   cd apps/backend
   export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
   export PATH="$JAVA_HOME/bin:$PATH"
   mvn spring-boot:run
   ```

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
# Build frontend apps
cd apps/client-web && npm run build
cd ../employee-web && npm run build

# Build backend
cd apps/backend && export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home && export PATH="$JAVA_HOME/bin:$PATH" && mvn test
cd apps/backend && export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home && export PATH="$JAVA_HOME/bin:$PATH" && mvn package

# Reset local DB data
docker compose down -v
```

## Notes
- This milestone is intentionally a foundation scaffold. The trading engine, account services, and reporting layer are not yet implemented.
- Docker is required for the primary and reporting databases, but it may not be available in every local environment.
- Market data remains simulated and clearly labeled as educational demo data.
