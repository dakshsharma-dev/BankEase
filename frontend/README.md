# BankEase frontend

Responsive React/Vite/TypeScript banking MVP. The API client uses `VITE_API_URL` (defaults to `/api`) and sends cookie-backed session credentials to protected endpoints. During local development, Vite proxies `/api` to `http://localhost:8080`.

## Scripts

- `npm run dev` — start the Vite development server
- `npm run build` — type-check and create a production bundle
- `npm run preview` — serve the production bundle locally

Available API calls are in `src/api.ts`, including authentication, dashboard, accounts, transactions, transfers, bills, loans, investments, and support/admin operations. The UI requires the backend to be running; API failures are shown in the app.
