# Refactoring Plan — StoreBook Epic Rollout

refactor_plan_doc: RP1.0
last_updated: "2026-07-13"
epics_covered: [e01, e02, e03]
goal: Extract technical debt blockers and structural improvements into actionable refactor tickets with clear before/after shapes

---

## A. HARD REQUIREMENT — SQLDelight Migration (Blocks ALL other work)

**SQLDelight is a hard requirement for all local database access going forward.** No future code may use raw `SQLiteOpenHelper`, Room DAOs, or hand-written SQL strings for data access.

### Current State — Why This Is Urgent

| File | DB Access Pattern | Risk Level |
|------|------------------|------------|
| `LegacySyncHelper.kt` | Raw `SQLiteOpenHelper` + manual rawQuery + ContentValues cursor extraction | **HIGH** — no type safety, empty onCreate/onUpgrade stubs |
| `SyncWorker.kt` (408 LOC) | Direct `SQLiteDatabase.rawQuery()` + 10+ `ContentValues` blocks + `insertWithOnConflict` | **HIGH** — string-compiled SQL, wrong-type mapping silent failures |
| `ExpiryCheckWorker.kt` | Room-backed `SupportSQLiteDatabase` adapter | **MEDIUM** — competing abstraction with SQLiteOpenHelper above |
| `ViewModelFactory.kt` | Room `RoomDatabase.openHelper.getWritableDb()` | **LOW-MEDIUM** — Room stable but creates 2nd DB layer alongside raw SQL |

- SQLDelight dependency **already declared** in `build.gradle.kts` (`libs.sqldelight.android.driver`)
- **Zero `.sq` query files** exist under `src/main/sqldelight/`
- **No generated DAOs**. All data access is manual strings. If a column rename happens anywhere, every rawQuery containing that column name must be found and fixed — no compiler help.

### Why SQLDelight Is Mandatory

1. **Compile-time query validation** — every raw string SQL in the app today is a runtime failure waiting to happen (misspelled column, missing WHERE clause, wrong result type). SQLDelight catches this at codegen time.
2. **Type-safe result mapping** — eliminates ~40 lines of manual cursor extraction per entity per file. `LegacySyncHelper.kt` alone has 8 SyncItem/SyncSale/etc constructors doing `getColumnIndexOrThrow`. One wrong index → wrong data in memory without compiler notice.
3. **Single source of truth for local schema** — `.sq` files ARE the schema definition. Generated Kotlin classes match exactly. Room's abstract annotations or hand-written CREATE TABLE strings diverge from reality silently.
4. **Migration safety** — SQLDelight manages migration scripts as versioned artifacts. Current `onUpgrade()` stub in LegacySyncHelper is an empty block `{}`. If a real migration lands now without tooling, data loss is guaranteed on every version bump for affected stores.
5. **KMP-ready foundation (future)** — shared module can reuse `.sq` files between Android + future iOS/client platforms. Sets the right foundation before e01/e02 rewrite even begins.

### RP-A0: Full SQLDelight Migration Plan

#### Step 1 — Define Schema (`.sq` files)

```
app/src/main/sqldelight/com/storebook/inventoryapp/
    StoreBook.sq          # CREATE TABLE DDL for all existing tables (items, sales, sale_items, udhaar, expenses, suppliers, purchases, purchase_items)
    SyncState.sq          # sync metadata table (last_sync_epoch, high_water_mark, etc.)
    FailedSyncQueue.sq    # new retry queue table (from DESIGN_PLAN §A.1)
```

Each `.sq` file defines:
- `CREATE TABLE` statements matching current SQLite schema exactly
- Named `SELECT`, `INSERT`, `UPDATE`, `DELETE` queries replacing every rawQuery call
- Parameterized inputs with explicit types (no interpolation)

#### Step 2 — Replace Data Access Layer, File by File

| Target File | Current Pattern | SQLDelight Replacement | Effort |
|------------|----------------|----------------------|--------|
| `LegacySyncHelper.kt` | Delete entire class. Queries become generated DAO calls on the shared driver | `db.StoreBookQueries.selectUnsyncedItems(storeId)`, `db.itemsQueries.updateSyncedFlag(id, true)` | 4h |
| `SyncWorker.kt` | Replace all rawQuery/ContentValues with typed method calls: `itemsQueries.saveItem(...)`, `salesQueries.getSalesAwaitingPush(...)` | ~10 ContentValues blocks → ~10 generated queries. Push phase helpers become parameterized `.sq` calls | 4-6h (highest complexity) |
| `ExpiryCheckWorker.kt` | Room adapter → SQLDelight driver. Query: `db.itemsQueries.getExpiredItems(threshold, storeId)` → auto-mapped List<Item> | Switch driver init, replace inline query string with generated method | 2h |
| `ViewModelFactory.kt` | Replace `RoomDatabase` with single SQLDelight `SqliteDriver` instance. Inject via constructor DI | One-time driver setup in App-level module, pass through factories | 1-2h |

#### Step 3 — Remove Competing Abstractions

- Remove Room dependency from `main` implementation scope (keep in test if needed for Robolectric, or remove entirely)
- Delete empty SQLiteOpenHelper onCreate/onUpgrade stubs — SQLDelight driver handles version management
- Consolidate single SQLDelight `SqliteDriver` instance at app DI level. All ViewModels/services share one driver reference.

#### Step 4 — Gradle Config Completeness Check

```kotlin
// Already present in build.gradle.kts:
implementation(libs.sqldelight.android.driver)

// Verify also added (if not already in libs.versions.toml):
ksp(libs.sqldelight.plugin)                              // KSP-based code generation
implementation(libs.sqldelight.coroutines.extensions)    // Kotlin coroutine + Flow support
```

#### Example — What Changes Look Like

```sql
-- SYNC_WORKER_SQFILE: Before (raw string in SyncWorker.kt line 185+)
val cv = ContentValues().apply {
    put("sale_id", saleId)
    put("item_id", itemId)
    put("item_name", itemName)
    put("quantity", quantity)
    ...
}
db.insertWithOnConflict("sale_items", null, cv, SQLiteDatabase.CONFLICT_REPLACE)

-- SQLDelight replacement (.sq file):
saveSaleItem(
  saleId: Long, itemId: Long, itemName: String, quantity: Double,
  unit: String, sellPrice: Double, isDeleted: Int, cloudId: String?,
  isSynced: Int, updatedAt: Long
);
INSERT OR REPLACE INTO sale_items (sale_id, item_id, item_name, quantity, unit,
    sell_price, is_deleted, cloud_id, is_synced, updated_at)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- Kotlin codegen: db.saleItemsQueries.saveSaleItem(saleId, itemId, itemName, ...)
```

**RP-A0 Total Effort**: ~13-16 hours (schema definition + 4 file migrations + driver setup + dependency verification)

### Impact on Other Refactor Tasks

| Task | Effect of RP-A0 |
|------|----------------|
| **BP-1** (SyncWorker split) | Done IN-LINE with migration. Instead of splitting raw SQL strings, split into typed DAO method calls — cleaner outcome |
| **BP-2** (parameterized query audit) | **Obsoleted.** SQLDelight enforces parameter binding automatically — string interpolation is structurally impossible in `.sq` files |
| **BP-3** (transaction wrapper) | Still needed but simpler. SQLDelight provides native `db.transaction { }` block that handles begin/commit/rollback: ```kotlin db.transaction { queries.addItem(...); queries.updateSyncState(...) } // auto-commits, rolls back on Exception ``` |

---

## B. High-Priority Refactors (After SQLDelight Migration)

These follow RP-A0 completion.

### BP-1: Extract SyncWorker Push/Pull into Separate Methods

**Current Problem**: `SyncWorker.kt` does push and pull in one monolithic `doWork()` block. Partial failures corrupt the entire batch.

```kotlin
// BEFORE (single method, no isolation)
override fun doBackgroundWork() {
    val items = dbHelper.getItemsToSync(storeId)
    for (item in items) { /* push to cloud */ }
    val remoteItems = dataConnect.pullItems(storeId)
    dbHelper.merge(remoteItems)
}

// AFTER (isolated stages, each with retry + persistence)
override fun doBackgroundWork() {
    try { val pending = failedSyncQueue.replayOverdue(); logReplay(pending) } catch(e: Exception) { /* enqueue remainder */ }
    pushLocalChanges()      // returns PushResult
    pullRemoteChanges()     // returns PullResult
    resolveConflicts()      // compare local vs cloud via ConflictResolver, apply LWW
}
```

**Scope**: e01-s1. **Effort**: 2-3 hours. Post-SQLDelight: all internal queries are generated DAO calls.

---

### BP-2: Extract Transaction Wrapper from Inline Boilerplate

**Current Problem**: Each item CRUD method repeats transaction begin/commit boilerplate inline — error-prone, one forgot the `finally` block.

Post-SQLDelight simplification:
```kotlin
// Use SQLDelight's native transaction { } — auto handles rollback on Exception
db.transaction {
    queries.addItem(...)
    queries.updateSyncState(...)
}
```

**Scope**: Inline with e02-s1. **Effort**: 1-2 hours (refactor all CRUD call sites to use `transaction { }` wrapper).

---

### BP-3: Consolidate ViewModel Sync State Observation

**Current Problem**: Multiple ViewModels (`InventoryViewModel`, `SalesViewModel`, `DashboardViewModel`) each poll Firebase auth state and check sync availability independently via their own lifecycle blocks. Duplicate logic, inconsistent UI feedback.

**Refactor**: Create a single `SyncStatusViewModel : ViewModel()` that exposes:
- `syncState: StateFlow<SyncState>` (enum: IDLE, SYNCING, PENDING, FAILED)
- `isOnline: StateFlow<Boolean>`
- `lastSyncTime: StateFlow<Long?>`

All other ViewModels observe this single source. Makes UI sync banner a global concern handled by one Composable observer in the navigation host.

**Scope**: Inline with e01-s4 (UI sync indicator). **Effort**: 3-4 hours.

---

### BP-4: Move Hardcoded Thresholds to Named Constants / Config Object

**Current Problem**: Magic numbers scattered across code — `500`ms timeout, retry intervals `5_000`/`30_000`/`7_200_000`/`86_400_000`, max photo size `5 * 1024 * 1024`.

**Refactor**: Create `StoreBookConfig` object with typed constants. Any threshold referenced in >1 place becomes a named constant.

**Scope**: Inline with e01/e02. **Effort**: 30 min (distributed as files are touched).

---

### BP-5: Extract Financial Calculations from Web Server Actions

**Current Problem**: Udhaar balance computation and sale-total aggregation are embedded inside server action route handlers. Route handlers do data-fetching AND calculation, making testing hard.

```typescript
// BEFORE — calculation tangled with fetch in route handler
export async function GET(request: Request) {
    const items = await db.query('SELECT ... FROM udhaar WHERE ...');
    let total = 0;
    for (const row of items) { total += row.type === 'debit' ? row.amount : -row.amount; }
    return Response.json({ balance: total });
}

// AFTER — pure function extracted, testable without DB
import { computeUdhaarBalance } from '@/lib/financialAudit';
export async function GET(request: Request) {
    const items = await db.query('SELECT ...');
    return Response.json({ balance: computeUdhaarBalance(items) });
}
```

**Scope**: e03-s1 pre-flight. **Effort**: 2 hours + unit test coverage.

---

## C. Additional Refactors (Lower Priority / Deferred)

### L-1: Shared KMP Domain Models

Move duplicate data classes (`Item`, `SaleRecord`, `UdhaarEntry`) from Android and web into the `shared/` KMP module for canonical type definitions. Currently duplicated — one side updates a field, the other drifts.

**Defer to**: After v1.4.0 stabilizes. Post-SQLDelight: `.sq`-generated models already provide a single truth for Android; web can share JSON schemas via Gradle plugin. Target: Phase 5 or v1.7.0+.

---

### L-2: Legacy Express.js Backend Retirement

`backend/` directory contains Express.js code with old routes (`/api/items`, `/api/sales`). Dead code — Data Connect + serverless functions replaced them entirely.

**Action**: Rename to `backend-legacy/` + README.md explaining supersession. Full deletion after 30 days post-v1.3.0 release.

**Effort**: 15 min. **Scope**: Any time, non-blocking.

---

### L-3: Ad Manager Initialization Guard for Subscribed Users

`AppOpenAdManager` calls `MobileAds.initialize()` unconditionally at startup regardless of subscription tier. Paid users waste battery initializing ad SDK.

**Refactor**: Check subscription status BEFORE ad init. If paid → skip entirely.

**Scope**: e05-s1 (subscription gating). **Effort**: 1 hour, deferred until e05 cycle.

---

## D. Refactor Execution Order & Dependencies

```
RP-A0 (SQLDelight migration) ─────────── BLOCKS ALL other refactors + all epic stories
    │ duration: 13-16h, replaces ALL raw SQLiteOpenHelper / Room access
    │ replaces LegacySyncHelper.kt entirely, rewrites SyncWorker query layer
    │
    ├── BP-1 (SyncWorker split) ────────▶ e01-s1
    │
    ├── BP-2 (transaction wrapper) ─────┤ e02 pre-flight → e02-s1
    │                                   │
    ├── BP-3 (SyncStatusVM) ────────────┤ e01-s4 (needs split worker first)
    │                                   │
    ├── BP-4 (constants extract) ───────┘ scattered across e01/e02 as files touched
    │
    └── BP-5 (calc extraction web) ─────▶ e03-s1 pre-flight → BLOCKS audit dashboard

[Deferrals — post MVP]
L-1  (KMP shared domain models)         ─▶ v1.7.0+
L-2  (legacy backend rename)            ─▶ any time, non-blocking
L-3  (ad init guard)                    ─▶ inline with e05-s1
```

**Combined pre-implementation effort**: ~22-28 hours total (RP-A0: 13-16h + BP-1..BP-5: 9-12h). Must be completed before epic story implementation begins. No code that mutates or queries local SQLite data may land until RP-A0 is green.
