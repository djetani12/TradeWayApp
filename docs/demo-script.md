# TradeWay Demo Script

## Goal
Provide a simple, repeatable local demonstration for the TradeWay simulation platform using the seeded demo accounts and educational market data.

## Prerequisites
- Node 22 installed and active
- Java 21 installed and active
- Docker running locally
- Project dependencies installed

## Start the environment
```bash
cd /Users/deepjetani/Desktop/TradeWay
nvm use 22
cp .env.example .env
docker compose up -d
```

## Start backend
```bash
cd apps/backend
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```

## Start customer web app
```bash
cd ../client-web
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

## Start employee web app
```bash
cd ../employee-web
npm install
npm run dev -- --host 0.0.0.0 --port 5174
```

## Demo flow
1. Open the customer portal at http://localhost:5173
2. Sign in with the seeded demo customer account
3. Review the dashboard cash, buying power, and open positions
4. Open the market catalog and place a simulated buy order
5. Confirm the trade is reflected in the recent order history and portfolio summary
6. Open the employee portal at http://localhost:5174
7. Sign in with the seeded admin or analyst account
8. Review KPI cards and operational health summaries
9. Toggle a user status through the admin controls to demonstrate suspension management

## Expected outcome
- The customer sees a live simulated account with portfolio movement.
- The employee dashboard reflects reporting and operational status changes.
- The app looks and behaves like a working demo, while remaining clearly non-production and educational in scope.
