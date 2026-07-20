# Verification Map — StoreBook Full Codebase Coverage

This document maps every logic domain in the StoreBook Android app to a verification epic with testable stories.

## Existing Epics Status (e01-e05)

| #{:Epic} | Title | Priority | Stories Done/Total | Status | Target |
|----------|-------|----------|--------------------|--------|--------|
| e01 | Sync Reliability — Push/Pull with Retry & Conflict Resolution | P0 | 4/4 | ✅ Complete | v1.3.0 |
| e02 | Inventory Core Stability — CRUD Safety & Low-Stock Alerts | P0 | 2/3 | 🟡 In Progress | v1.3.0 |
| e03 | Billing Accuracy & Financial Audit — Sales, Udhaar, Expenses | P1 | 1/4+ | 🟢 Partial | v1.4.0 |
| e04 | Web Dashboard UX & Realtime Analytics — Charts, Permissions | P2 | 0/3 | 🔴 Not Started | v1.5.0 |
| e05 | Subscription Monetization & Feature Gating — Razorpay, IAP, Ads | P2 | 0/3 | 🔴 Not Started | v1.6.0 |

## New Verification Epics (e06-e17) — Logic Verification per Domain

### P0 — Must Verify (Data Integrity & App Stability)

| #{:Epic} | Domain Covered | What It Verifies | Stories |
|----------|---------------|------------------|---------|
| **e06** | 🧮 Sales Billing Engine | Sale total formula, PDF invoice accuracy, Quotation→Sale conversion atomicity, sale deletion cascade | 4 |
| **e08** | 🔐 Auth & Session Security | Phone OTP flow, Email/Pass auth, session persistence, logout clears all state | 4 |
| **e10** | 🛠️ Utility Functions | CurrencyUtils formatting (₹), LanguageManager switching, NetworkMonitor transitions, Share/Review/AppUpdate helpers | 4 |
| **e11** | 📊 ViewModel State Mgmt | All 8 ViewModels reactive updates, ViewModelFactory unsafe cast fix, Dashboard totals react to mutations | 5 |
| **e13** | 💾 SQLDelight Data Layer | All .sq queries return correct data, upserts generate unique local IDs, soft-delete filters, FailedSyncQueue retry logic | 4 |
| **e14** | 🔗 Auth Screen + Notifications | Phone/OTP login flow, email/password validation, session persistence, notification channel creation at startup | 3 |
| **e15** | ⏰ ExpiryCheckWorker | Expiry detection within 7-day window, low-stock threshold alerts, worker scheduling respects battery limits | 3 |
| **e16** | 📦 Purchase & Supplier | Purchase adds stock correctly to inventory, purchase history sorted/filterable, supplier CRUD with FK protection | 3 |
| **e17** | 🚀 App Launch & Nav | Splash→Auth routing, config change survival (rotation), offline-first mode works fully without network | 3 |
| **e30** | 📸 UI Snapshot Testing | Prevent layout regressions using Paparazzi/Roborazzi for Dashboard, Sales, Udhaar, and Inventory screens | 3 |
| **e31** | 🧪 Unit & DB Testing | JUnit4 + Turbine for Flow testing, and SQLDelight JdbcSqliteDriver for in-memory DB operations | 3 |
| **e32** | ⚙️ CI/CD & Linting | ktlint, detekt, and GitHub Actions configuration to block PRs on failed tests or lint rules | 2 |

### P1 — Should Verify (Financial Records & Gating)

| #{:Epic} | Domain Covered | What It Verifies | Stories |
|----------|---------------|------------------|---------|
| **e07** | 💰 Udhaar Ledger + Expense/Purchase | Balance = outstanding - payments, expense category grouping, supplier CRUD linked to purchases | 4 |
| **e09** | 📄 PDF Generation + Excel Export | InvoicePdfGenerator accuracy, UdhaarPdfGenerator running balance, ExcelExporter valid .xlsx files | 3 |
| **e12** | 💳 Subscription Gating + Ads | Feature gating blocks free users, AppOpenAds only for non-subscribers, PlayBillingManager purchase lifecycle | 4 |

## Coverage Matrix — What Each Epic Verifies Against Code Modules

```
Product Module              │ e06│e07│e08│e09│e10│e11│e12│e13│e14│e15│e16│e17
────────────────────────────┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───
Auth (Firebase OTP/Email)   │   │   │ ✅│   │   │   │   │   │✅   │   │   │✅   
Android POS (Billing UI)    │✅  │   │   │   │   │✅  │   │   │   │   │   │✅   
Inventory CRUD              │✅  │   │   │   │   │✅  │   │✅  │   │✅  │   │   
Sales & Billing (calc)      │✅  │✅  │   │   │   │   │   │   │   │   │   │   
Udhaar Credit Ledger        │   │✅  │   │✅  │   │✅  │   │   │   │   │   │   
Expense Tracking            │   │✅  │   │   │   │   │   │   │   │   │   │   
Purchases + Supplier        │   │✅  │   │   │   │   │   │   │   │✅  │   │   
Sync Engine (push/pull)     │   │   │   │   │   │   │   │✅  │   │   │   │   
Web Dashboard               │   │   │   │   │   │   │   │   │   │   │   │   
Subscriptions / Ads         │   │   │   │   │   │   │✅  │   │   │   │   │   
SQLDelight Schema/Queries   │   │   │   │   │   │   │   │✅  │   │   │   │   
Utility Helpers             │   │   │   │   │✅  │   │   │   │   │   │   │   
Notification Channel        │   │   │   │   │   │   │   │   │✅  │   │   │   
PDF/Excel Generation        │   │   │   │✅  │   │   │   │   │   │   │   │   
```

## Remaining Gaps — Areas Not Fully Covered

These need to be added as additional epics (e18+):

| Area | File/Class | Current Gap |
|------|-----------|-------------|
| 🌐 Web Dashboard Server Actions | web/src/app/api/* | No verification of Next.js server actions returning correct aggregates |
| 🔍 Compose UI Components | ui/components/*.kt | AutocompleteDropdown, search functionality not verified |
| 🔒 Security Utils | SecurityUtils.kt | Encryption key generation, secure storage reading/writing |
| 📱 Play Billing Manager | PlayBillingManager.kt | Pending purchases enablement, SKU purchase flow end-to-end |
| 🌐 Network Monitor edge cases | NetworkMonitor.kt | Flaky connectivity, rapid connect/disconnect cycles |
