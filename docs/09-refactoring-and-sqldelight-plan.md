# 09 — Architecture Refactoring & SQLDelight Migration Plan

## Objective
Migrate the StoreBook Android application from raw SQLite (`StoreBookDbHelper`) to Kotlin Multiplatform's **SQLDelight**, while simultaneously breaking down the monolithic `StoreBookRepository` (1880 lines) and `StoreBookViewModel` (47KB) into a clean, domain-driven architecture.

This plan is designed to be executed incrementally to prevent application breakage and ensure a smooth transition.

---

## Phase 1: Database Migration to SQLDelight (KMP `shared` module)
Currently, SQL queries are hardcoded strings in `StoreBookRepository.kt`. SQLDelight will replace these with compile-time checked `.sq` files.

### 1.1 Gradle Configuration
- **Action**: Update `shared/build.gradle.kts` to fully configure the SQLDelight plugin.
- **Details**: Define the database name (e.g., `StoreBookDatabase`) and the package where the classes will be generated.

### 1.2 Define `.sq` Schema Files
Instead of a single SQLite schema file, split the tables into their respective domain `.sq` files in `shared/src/commonMain/sqldelight/com/storebook/inventoryapp/shared/database/`:
1. `Items.sq`: Contains `CREATE TABLE items`, `insertItem`, `getActiveItems`, `updateStock`.
2. `Sales.sq`: Contains `CREATE TABLE sales`, `CREATE TABLE sale_item_details`, `insertSale`, `getSalesHistory`.
3. `Udhaar.sq`: Contains `CREATE TABLE udhaar_entries`, `insertUdhaar`, `getPartyBalance`.
4. `Users.sq`: Contains `CREATE TABLE users`, `CREATE TABLE auth_session`.

### 1.3 Implement the Driver Factory
- **Action**: Finalize the `DatabaseDriverFactory` in the `shared/src/androidMain` folder using `AndroidSqliteDriver`.
- **Action**: Handle the migration path from the old `storebook.db` to ensure existing user data is preserved by hooking the old SQLite file into the new SQLDelight driver.

---

## Phase 2: Repository Splitting (Domain Repositories)
We will replace the 1,880-line `StoreBookRepository.kt` by extracting its logic into focused, domain-specific repositories within the `shared` module.

### 2.1 Create Domain Repositories
Create the following classes in the `shared` module. Each will accept the generated SQLDelight `StoreBookDatabase` as a dependency:
- `InventoryRepository`: Manages `Items.sq` queries (CRUD, stock checks).
- `SalesRepository`: Manages `Sales.sq` (Checkout, invoicing). Requires a cross-domain dependency on `InventoryRepository` for stock deduction.
- `UdhaarRepository`: Manages `Udhaar.sq` (Party balances, credit).
- `SyncRepository`: Centralizes the Firebase Data Connect synchronization logic, calling the other repositories to reconcile local/cloud data.

### 2.2 The Façade Pattern (Safety Net)
To avoid immediately breaking the entire Android UI layer, `StoreBookRepository.kt` will temporarily be kept as a **Façade**. 
- **Action**: Rewrite the methods inside `StoreBookRepository.kt` to simply delegate calls to the new domain repositories.
- *Example*: `fun getActiveItems() = inventoryRepository.getActiveItems()`
- **Benefit**: The UI will continue compiling and functioning while the data layer is swapped entirely to SQLDelight under the hood.

---

## Phase 3: ViewModel Splitting (Android UI Layer)
Once the data layer is stable, we break apart the 47KB `StoreBookViewModel.kt`.

### 3.1 Extract Domain ViewModels
Create distinct ViewModels to manage state for specific screens:
- `InventoryViewModel`: State for `InventoryScreen`, `QuickRestock`.
- `SalesViewModel`: State for `SalesScreen` (POS), `SalesHistoryScreen`, `SalesAnalyticsScreen`.
- `UdhaarViewModel`: State for `UdhaarScreen`, `SupplierLedgerScreen`.
- `AuthViewModel`: State for `AuthScreen`, `DashboardScreen` (user status).

### 3.2 Update Jetpack Compose UI Signatures
- **Action**: Update the `AppNavigation.kt` to instantiate these specific ViewModels instead of a single `storeBookViewModel`.
- **Action**: Refactor the 15+ Jetpack Compose screens to accept their respective ViewModel. For screens requiring multiple domains (like the POS needing both Sales and Inventory), we will pass only the specific state flows needed, or pass multiple ViewModels scoped to the NavGraph.

### 3.3 Remove the Façade
- **Action**: Once all screens are using the new ViewModels, the legacy `StoreBookViewModel` and the `StoreBookRepository` Façade can be safely deleted.

---

## Phase 4: Fix Edge Cases during Refactor

1. **Transaction Boundaries**: Currently, saving a sale and updating Udhaar happens in one raw SQLite transaction. In SQLDelight, this requires using the `database.transaction {}` block. We must ensure the `SalesRepository` can execute multi-table transactions safely.
2. **Offline Sync Reconciliation**: The `SyncWorker` must be updated to use the new `SyncRepository` instead of the legacy helper, ensuring background processes run smoothly on the new architecture.

## Sequence of Execution
**Step A**: Implement SQLDelight `.sq` files and generate the Database classes. *(Safe, no existing code modified)*
**Step B**: Build the Domain Repositories in `shared`. *(Safe, no existing code modified)*
**Step C**: Convert `StoreBookRepository.kt` to a Façade. *(Medium risk, touches Android data layer)*
**Step D**: Split ViewModels and update Compose UI. *(High risk, requires extensive UI testing)*
**Step E**: Delete legacy files and finalize.
