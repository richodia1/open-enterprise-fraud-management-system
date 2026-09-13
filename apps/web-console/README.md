# OEFMS Web Console

React and TypeScript analyst/admin console for the Open Enterprise Fraud Management System.

## Local Development

```bash
cp .env.example .env.local
npm install
npm run dev
```

The console expects the API Gateway at `VITE_OEFMS_API_BASE_URL`. The default local value is:

```text
http://localhost:8080
```

Run the local backend stack before using live screens:

```bash
../../scripts/smoke-local.sh --skip-build --start-stack --gateway --event-flow
```

## Checks

```bash
npm run lint
npm test
npm run build
```

## First Screens

- Dashboard
- Cases
- Alerts
- Rule Management
- Configuration Management
- List Management
- Identity
- Audit Logs
- Reports
- System Health
