# 07 — Edge Cases & Technical Debt

## Critical Issues

## Resolved Issues

### ✅ Data Connect Auth Hardening
- **`SyncUser` Public Auth**: Fixed. Modified `dataconnect/connector/mutations.gql` to use `@auth(level: USER, expr: "auth.uid == id")`. Users can now only synchronize their own user record, preventing arbitrary user creation.
- **Role-Based Auth (Custom Claims Pipeline)**: Implemented. The Next.js `login()` action in `actions.ts` now automatically queries the Data Connect `User` record and injects `role` and `stores` directly into the Firebase Auth Custom Claims (`auth.token.role` and `auth.token.stores`).
- *Note*: Data Connect schema-level CEL expressions (e.g., `expr: "auth.token.stores.has($storeId)"`) can now be incrementally rolled out across queries/mutations without blocking the flow, as the claim pipeline is now active.

### ✅ Server Actions Implemented
- **`createStore()`**: Fully implemented with Data Connect store creation and User `stores` array appending.
- **`updateUserRole()`**: Implemented to change roles directly in Data Connect and forcibly revoke the user's active session tokens via Firebase Admin Auth.
- **`archiveOldData()`**: Implemented using Data Connect `_deleteMany` bulk mutations across all primary tables, deleting records older than the cutoff date where `isDeleted == true`.

### ✅ Tax Calculation Parity
- **Fixed**: Ported `BillingEngine.kt` to `web/src/lib/BillingEngine.ts`.
- **Status**: The Next.js Web POS now correctly calculates CGST, SGST, and IGST dynamically based on item-specific `taxRate` and `hsnCode`, matching Android functionality.
- **Fixed**: Removed the hardcoded `taxRate = 18.0` SQLite update in `StoreBookRepository.kt` which was incorrectly overwriting valid 0% (tax-exempt) items.

### ✅ POS Invoice PDF Parity
- **Fixed**: Implemented `InvoicePdfGenerator.ts` using `jspdf` and `jspdf-autotable`.
- **Status**: Web POS now automatically generates and downloads an itemized PDF invoice (including accurate tax breakdowns) upon successful checkout, matching Android's A4/Thermal printing capability.

### ✅ Web Offline Caching
- **Fixed**: Integrated Redux `inventorySlice` caching into `SalesPOS.tsx`.
- **Status**: The Web POS now reads from the Redux-persisted inventory cache immediately (offline-like responsiveness) and skips the loading spinner if the cache is < 5 minutes old.

---

## Edge Cases

### 1. Stock Oversell Race Condition
**Location**: `StoreBookRepository.recordSale()` (lines 240-248)
On Android, stock check + deduction happens within a SQLite transaction. However, if two devices are selling the same item concurrently, both will succeed locally (each has their own SQLite). Cloud reconciliation via `SyncWorker` will overwrite with last-writer-wins, potentially resulting in negative stock.

**Mitigation**: None currently. Would require a cloud-side stock guard or optimistic locking.

---

### 2. Customer Name Matching (Udhaar)
**Location**: `StoreBookRepository.getUdhaarBalances()`, `undoSale()`
Udhaar entries are grouped by `customer_name` (TEXT). Name matching is case-sensitive. The `standardizeCustomerNames()` function normalizes to title-case, but this must be run manually.

**Risk**: "Raj Kumar" and "raj kumar" treated as different customers.

---

### 3. Udhaar-Sale Link is Fragile
**Location**: `StoreBookRepository.undoSale()` (line 358)
Sale-to-udhaar linking uses `notes LIKE "Sale bill #$saleId%"`. If the note format changes or is manually edited, the undo operation will fail to find associated credit entries.

**Recommendation**: Add an explicit `sale_id` foreign key to the `udhaar` table.

---

### 4. Quotation-to-Sale Conversion Creates Duplicate Stock Impact
**Location**: `QuotationScreen.kt`
When converting a quotation (ESTIMATE) to a sale, a new sale is recorded with stock deduction. If the original quotation's items have since been sold (stock reduced), the conversion may trigger a stock oversell error.

**UNCONFIRMED** — verify the exact conversion flow in `QuotationScreen.kt`.

---

### 5. Downgrade Wipes Local Data
**Location**: `StoreBookDbHelper.onDowngrade()` (drops all tables and recreates)
If a user downgrades the app (e.g., from dev build to production), all local data is destroyed. Recovery depends on cloud data being available and SyncWorker successfully pulling it back.

---

### 6. EncryptedSharedPreferences Fallback
**Location**: `SecurityUtils.kt` (line 47)
On failure, falls back to unencrypted SharedPreferences. This silently degrades security without user notification. `last_sync_timestamp` and other sensitive metadata would be stored in plaintext.

---

### 7. Country Code Duplicates
**Location**: `web/src/lib/constants.ts`
Several country codes appear multiple times (e.g., `+61` for AU/CX/CC, `+1` for CA/US, `+262` for YT/RE). The dropdown uses array index for uniqueness but this could cause confusion.

---

## Technical Debt

### 1. Monolithic ViewModel and Repository (Android)
**Location**: `StoreBookViewModel.kt` (~47KB) and `StoreBookRepository.kt` (~1880 lines).
**Planned Refactor**: Split into domain-specific modules (`InventoryRepository`, `SalesRepository`, `UdhaarRepository`) and corresponding ViewModels. A façade pattern will be used to maintain existing UI compatibility while separating internal concerns.

### 2. Legacy Backend (`backend/server.js`)
The Express.js + SQLite server is likely a development remnant. It reads from a local `storebook.db` file and provides REST endpoints that are not used in production (Data Connect has replaced this). Consider removing.

### 3. `AppResponse.kt` Ad-Config Model
The 129-field ad-config response model (`AppResponse.kt`) suggests heavy ad monetization infrastructure. Many fields (`quereka_*`, `atme_*`, `facebook_*`) indicate multiple ad networks. This adds complexity to the build and may impact performance.

### 4. KMP Module Underutilized
The `shared/` KMP module contains domain models and a skeleton loader but the database driver factory is present but no actual shared database code. SQLDelight integration appears started but not completed — Android still uses raw SQLite (`StoreBookDbHelper`).

### 5. No Database Migration Tests
No tests exist for the SQLite migration path (versions 1-9). If a migration step fails, the app falls back to dropping all tables.

### 6. `fetchMoreData()` Returns Empty Array
**Location**: `web/src/app/actions.ts` (lines 182-186)
The pagination stub always returns `[]`, breaking any infinite-scroll UI that depends on it.

### 7. Ad-Hoc SQL in Repository
All SQL queries are hand-written strings with parameter interpolation. No query builder, no compile-time SQL validation. Risk of SQL injection is mitigated by parameterized queries, but query correctness relies on runtime testing.

### 8. No Integration Tests
Neither the Android nor web platform has automated integration tests for the sync flow, CRUD operations, or auth flows.

---

## Unresolved Questions

1. **What triggers the ExpiryCheckWorker?** It exists in `workers/` but its scheduling mechanism was not found during this audit.
2. **Are Firestore rules (`firestore.rules`) still relevant?** The project has migrated to Data Connect, but Firestore rules exist.
3. **Is the `backend/` directory used in any deployment?** Or is it purely a local development artifact?
4. **PWA manifest**: `/manifest.json` is referenced in the layout but its contents were not audited.
5. **Stripe/Razorpay integration for web subscriptions**: UNCONFIRMED — no payment gateway code found in web source.

