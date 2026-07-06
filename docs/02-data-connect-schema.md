# 02 — Firebase Data Connect Schema

## Configuration

- **Service ID**: `store-book`
- **Location**: `us-central1`
- **Database**: `storebookdb` on Cloud SQL instance `storebook-sql`
- **Schema file**: `dataconnect/schema/schema.gql`
- **Connector**: `storebook-connector`
- **Generated SDKs**:
  - Kotlin → `app/src/main/java/com/storebook/inventoryapp/dataconnect/`
  - TypeScript → `web/src/dataconnect/`

---

## Queries (22 total)

### Delta Sync Queries

| Query | Args | Returns | Tables | Auth | Android Caller | Web Caller |
|---|---|---|---|---|---|---|
| `SyncItems` | `storeId: String!, lastSync: Float!` | Items where `updatedAt > lastSync` | `Item` | `@auth(level: USER)` | `SyncWorker.kt` → `connector.syncItems.execute()` | — |
| `SyncSales` | `storeId, lastSync` | Sales delta | `Sale` | USER | `SyncWorker.kt` | — |
| `SyncSaleItems` | `storeId, lastSync` | SaleItemDetails delta | `SaleItemDetail` | USER | `SyncWorker.kt` | — |
| `SyncUdhaars` | `storeId, lastSync` | UdhaarEntries delta | `UdhaarEntry` | USER | `SyncWorker.kt` | — |
| `SyncExpenses` | `storeId, lastSync` | ExpenseEntries delta | `ExpenseEntry` | USER | `SyncWorker.kt` | — |
| `SyncSuppliers` | `storeId, lastSync` | Suppliers delta | `Supplier` | USER | `SyncWorker.kt` | — |
| `SyncPurchases` | `storeId, lastSync` | Purchases delta | `Purchase` | USER | `SyncWorker.kt` | — |
| `SyncPurchaseItems` | `storeId, lastSync` | PurchaseItemDetails delta | `PurchaseItemDetail` | USER | `SyncWorker.kt` | — |
| `SyncItemBatches` | `storeId, lastSync` | ItemBatches delta | `ItemBatch` | USER | `SyncWorker.kt` | — |

### Active Data Queries (Paginated)

| Query | Args | Returns | Auth | Android Caller | Web Caller |
|---|---|---|---|---|---|
| `GetActiveItems` | `storeId, limit?, offset?` | Active items (not deleted), ordered by `updatedAt DESC` | USER | — | `ItemsClient.tsx` |
| `GetActiveSales` | `storeId, type?, limit?, offset?` | Active sales by type | USER | — | `SalesClient.tsx`, `QuotationsClient.tsx` |
| `GetActiveSaleItems` | `storeId` | All active sale items | USER | — | `DashboardClient.tsx`, `ReportsClient.tsx` |
| `GetActiveUdhaars` | `storeId, limit?, offset?` | Active udhaar entries | USER | — | `UdhaarClient.tsx` |
| `GetActiveExpenses` | `storeId, limit?, offset?` | Active expenses | USER | — | `ExpensesClient.tsx` |
| `GetActiveSuppliers` | `storeId` | Active suppliers | USER | — | `ItemsClient.tsx` (restock) |

### Count Queries (Lightweight)

| Query | Args | Returns | Auth | Purpose |
|---|---|---|---|---|
| `GetItemsCount` | `storeId` | `[{id}]` | USER | Total count for pagination |
| `GetSalesCount` | `storeId, type?` | `[{id}]` | USER | Sales count for pagination |
| `GetUdhaarEntriesCount` | `storeId` | `[{id}]` | USER | Udhaar count |
| `GetExpenseEntriesCount` | `storeId` | `[{id}]` | USER | Expense count |

### User / Admin Queries

| Query | Args | Returns | Auth | Caller |
|---|---|---|---|---|
| `GetUser` | `id: String!` | Single user record | USER | `session.ts` (server), `AuthScreen.kt` |
| `GetStoresPaginated` | — | All stores | USER | `actions.ts`, `AdminDashboardClient.tsx` |
| `GetUsersPaginated` | — | All users | USER | `actions.ts`, `UsersClient.tsx` |
| `GetGlobalSettings` | — | All settings | USER | `SettingsClient.tsx` |
| `GetAdminAuditLogs` | — | All audit logs | USER | `DataClient.tsx` |
| `GetAnnouncements` | — | All announcements | USER | `SettingsClient.tsx` |
| `GetPromoCodes` | — | All promo codes | USER | `BillingClient.tsx` |

---

## Mutations (23 total)

### Sync/Upsert Mutations (Push from Offline)

| Mutation | Auth | Tables | Android Caller |
|---|---|---|---|
| `SyncItem` | USER | `Item` (upsert) | `SyncWorker.kt` |
| `SyncSale` | USER | `Sale` (upsert) | `SyncWorker.kt` |
| `SyncSaleItem` | USER | `SaleItemDetail` (upsert) | `SyncWorker.kt` |
| `SyncUdhaar` | USER | `UdhaarEntry` (upsert) | `SyncWorker.kt` |
| `SyncExpense` | USER | `ExpenseEntry` (upsert) | `SyncWorker.kt` |
| `SyncSupplier` | USER | `Supplier` (upsert) | `SyncWorker.kt` |
| `SyncPurchase` | USER | `Purchase` (upsert) | `SyncWorker.kt` |
| `SyncPurchaseItem` | USER | `PurchaseItemDetail` (upsert) | `SyncWorker.kt` |
| `SyncItemBatch` | USER | `ItemBatch` (upsert) | `SyncWorker.kt` |

### Soft Delete Mutations

| Mutation | Auth | Tables |
|---|---|---|
| `SoftDeleteItem` | USER | `Item` (sets `isDeleted=true`) |
| `SoftDeleteSale` | USER | `Sale` (sets `isDeleted=true`) |
| `SoftDeleteUdhaar` | USER | `UdhaarEntry` |
| `SoftDeleteExpense` | USER | `ExpenseEntry` |

### User/Store Mutations

| Mutation | Auth | Tables | Caller |
|---|---|---|---|
| `SyncUser` | **PUBLIC** | `User` (upsert) | `AuthScreen.kt` (first login) |
| `UpdateUser` | USER | `User` (partial update) | Subscription management |
| `CreateUser` | USER | `User` (upsert) | `actions.ts` → `createStaffAccount()` |
| `SyncStore` | USER | `Store` (upsert) | `AuthScreen.kt` (store creation) |
| `UpdateStore` | USER | `Store` (partial update) | Subscription update |

### Admin Mutations

| Mutation | Auth | Tables | Web Caller |
|---|---|---|---|
| `ToggleStoreStatus` | USER | `Store` | `actions.ts` → `toggleStoreStatus()` |
| `PurgeStore` | USER | `Store` (sets `isActive=false`) | `actions.ts` → `purgeStoreData()` |
| `UpsertGlobalSetting` | USER | `GlobalSetting` | `SettingsClient.tsx` |
| `CreateAdminAuditLog` | USER | `AdminAuditLog` (insert) | `actions.ts` |
| `UpsertAnnouncement` | USER | `Announcement` | `SettingsClient.tsx` |
| `DeleteAnnouncement` | USER | `Announcement` (hard delete) | `SettingsClient.tsx` |
| `UpsertPromoCode` | USER | `PromoCode` | `BillingClient.tsx` |
| `DeletePromoCode` | USER | `PromoCode` (hard delete) | `BillingClient.tsx` |

---

## Auth Directives

- **All queries and most mutations**: `@auth(level: USER)` — requires a signed-in Firebase Auth user.
- **Exception**: `SyncUser` mutation uses `@auth(level: PUBLIC)` — allows first-time user creation without prior DB record.
- **No role-based auth at Data Connect level** — role checks are performed application-side (Android ViewModel + Web `getSession()` + server actions).

---

## Generated SDK Usage Patterns

### Android (Kotlin)
```kotlin
// Access connector singleton
val connector = StorebookConnectorConnector.instance

// Execute mutation
val result = connector.syncItem.execute(
    id = item.id.toString(), storeId = storeId, name = item.name, ...
) { photoPath = item.photoPath; hsnCode = item.hsnCode }

// Execute query
val itemsRes = connector.syncItems.execute(storeId, lastSync.toDouble())
val items = itemsRes.data.items  // typed list
```

### Web (TypeScript)
```typescript
import { getActiveItems, ... } from '@/dataconnect';

// Execute query via generated SDK
const result = await getActiveItems({ storeId, limit: 50, offset: 0 });
```

**Caching**: Android relies on local SQLite as the cache; queries only run during sync. Web uses Redux + localStorage (`redux-persist`) for client-side caching of items, cart, and udhaar.

---

## Direct Firebase Calls (Bypassing Data Connect)

| Call | Platform | Purpose | File |
|---|---|---|---|
| `FirebaseAuth.signInWithPhoneNumber()` | Android, Web | Phone OTP authentication | `AuthScreen.kt`, `login/page.tsx` |
| `FirebaseAuth.signInWithCredential(GoogleAuthProvider)` | Android | Google sign-in | `AuthScreen.kt` |
| `FirebaseAuth.signInWithEmailAndPassword()` | Web | Staff login (virtual email) | `login/page.tsx` |
| `adminAuth.createSessionCookie()` | Web (server) | Create session cookie | `actions.ts` |
| `adminAuth.createUser()` | Web (server) | Create staff Firebase Auth account | `actions.ts` |
| `adminAuth.revokeRefreshTokens()` | Web (server) | Revoke user sessions | `actions.ts` |
| `FirebaseDatabase.getReference("store_updates")` | Android | Write sync-ping timestamp | `SyncWorker.kt` |
| `getDatabase().ref("store_updates")` | Web | Listen for sync pings | `DashboardClient.tsx` |
