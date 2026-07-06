# 07 — Open Questions, Edge Cases & Technical Debt

## Critical Issues

### 1. No Role-Based Auth at Data Connect Level
**Risk**: HIGH  
**Location**: `dataconnect/connector/queries.gql`, `mutations.gql`  
All queries and mutations use `@auth(level: USER)`, meaning any authenticated user can read or write any store's data by passing a different `storeId`. Role checks only happen application-side (in ViewModel/server actions).

**Recommendation**: Add `@auth` directives with custom claims or implement a `@check` directive against the User record to enforce that `auth.uid` owns/belongs to the requested `storeId`.

---

### 2. `SyncUser` Mutation is PUBLIC Auth
**Risk**: MEDIUM  
**Location**: `dataconnect/connector/mutations.gql` — `SyncUser`  
This mutation has `@auth(level: PUBLIC)`, allowing unauthenticated creation of user records. While necessary for first-time registration, it means anyone with the endpoint could create arbitrary user records.

**Recommendation**: Consider a two-step flow where user creation happens server-side via Admin SDK, or add a `@check` that `auth.uid` matches the `id` being created.

---

### 3. Web `createStore()` is Not Implemented
**Risk**: MEDIUM  
**Location**: `web/src/app/actions.ts` (line 61-65)  
The `createStore()` action returns `{ success: false, error: "Not implemented in DataConnect yet." }`. Web users cannot create new stores.

**UNCONFIRMED** — verify whether store creation from web is a product requirement.

---

### 4. Web `updateUserRole()` is Empty
**Risk**: MEDIUM  
**Location**: `web/src/app/actions.ts` (line 180)  
The function has an empty body: `export async function updateUserRole(userId: string, role: string, storeId: string | null) {}`. Admin user management is non-functional.

---

### 5. `archiveOldData()` is a Mock
**Risk**: LOW (admin-only feature)  
**Location**: `web/src/app/actions.ts` (lines 67-71)  
Returns a hardcoded `{ success: true, count: 1245 }`. No actual data archival occurs.

---

## Platform Divergences

### Tax Calculation
| Aspect | Android | Web |
|---|---|---|
| GST Engine | ✅ `BillingEngine.kt` (BigDecimal precision) | ❌ Not implemented |
| Invoice PDF | ✅ `InvoicePdfGenerator.kt` | ❌ Not implemented |
| Tax on sale | ✅ Applied during checkout | ❌ No checkout flow creates taxes |

**Impact**: If web sales creation is ever added, taxes will not be calculated. The web currently only displays pre-calculated totals from cloud-synced data.

### POS (Point of Sale)
| Feature | Android | Web |
|---|---|---|
| Cart management | ViewModel state | Redux `cartSlice` |
| Quick Cash Sale | ✅ (dummy item id=0) | UNCONFIRMED |
| Stock oversell guard | ✅ Exception thrown | UNCONFIRMED on web |
| Auto-udhaar on credit sale | ✅ | UNCONFIRMED |
| Invoice generation | ✅ PDF | ❌ |

### Data Access
| Feature | Android | Web |
|---|---|---|
| Data source | Local SQLite (offline-first) | Direct Data Connect queries (online-required) |
| Offline capability | ✅ Full | ❌ None (except Redux cache) |
| Pagination | SQLite LIMIT/OFFSET | Data Connect limit/offset |
| Real-time updates | RTDB listener → SyncWorker | RTDB listener → refetch |

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

### 1. Monolithic ViewModel (Android)
`StoreBookViewModel.kt` is ~47KB and manages state for the entire application. It should be split into per-feature ViewModels (InventoryViewModel, SalesViewModel, UdhaarViewModel, etc.).

### 2. Monolithic Repository (Android)
`StoreBookRepository.kt` is 1880 lines with all CRUD + sync operations. Consider splitting by domain (InventoryRepository, SalesRepository, etc.).

### 3. Legacy Backend (`backend/server.js`)
The Express.js + SQLite server is likely a development remnant. It reads from a local `storebook.db` file and provides REST endpoints that are not used in production (Data Connect has replaced this). Consider removing.

### 4. `AppResponse.kt` Ad-Config Model
The 129-field ad-config response model (`AppResponse.kt`) suggests heavy ad monetization infrastructure. Many fields (`quereka_*`, `atme_*`, `facebook_*`) indicate multiple ad networks. This adds complexity to the build and may impact performance.

### 5. KMP Module Underutilized
The `shared/` KMP module contains domain models and a skeleton loader but the database driver factory is present but no actual shared database code. SQLDelight integration appears started but not completed — Android still uses raw SQLite (`StoreBookDbHelper`).

### 6. No Database Migration Tests
No tests exist for the SQLite migration path (versions 1-9). If a migration step fails, the app falls back to dropping all tables.

### 7. Hard-Coded Tax Rate Seed
**Location**: `StoreBookRepository.standardizeCustomerNames()` (line 1067-1068)  
```kotlin
db.execSQL("UPDATE items SET tax_rate = 18.0 WHERE tax_rate = 0.0")
```
This blindly sets all zero-tax items to 18% GST, which is incorrect for many product categories (food: 5%, exempt: 0%).

### 8. `fetchMoreData()` Returns Empty Array
**Location**: `web/src/app/actions.ts` (lines 182-186)  
The pagination stub always returns `[]`, breaking any infinite-scroll UI that depends on it.

### 9. Ad-Hoc SQL in Repository
All SQL queries are hand-written strings with parameter interpolation. No query builder, no compile-time SQL validation. Risk of SQL injection is mitigated by parameterized queries, but query correctness relies on runtime testing.

### 10. No Integration Tests
Neither the Android nor web platform has automated integration tests for the sync flow, CRUD operations, or auth flows.

---

## Unresolved Questions

1. **Is web POS actively used?** The SalesPOS component exists but it's unclear if it creates real sales or is read-only.
2. **What triggers the ExpiryCheckWorker?** It exists in `workers/` but its scheduling mechanism was not found during this audit.
3. **Are Firestore rules (`firestore.rules`) still relevant?** The project has migrated to Data Connect, but Firestore rules exist.
4. **Is the `backend/` directory used in any deployment?** Or is it purely a local development artifact?
5. **PWA manifest**: `/manifest.json` is referenced in the layout but its contents were not audited.
6. **Stripe/Razorpay integration for web subscriptions**: UNCONFIRMED — no payment gateway code found in web source.
