# Refactoring Plan — StoreBook Epic Rollout

refactor_plan_doc: RP1.2
last_updated: "2026-07-20"
implementation_review: "Codebase verified against actual source files"
epics_covered: [e01, e02, e03]
goal: Extract technical debt blockers and structural improvements into actionable refactor tickets with clear before/after shapes

---

## Status Summary

| ID | Refactor | Priority | Result | Verified |
|----|----------|--------- | ------ | -------- |
| RP-A0 | SQLDelight Migration | HARD REQUIREMENT | ✅ COMPLETED | 2026-07-20 |
| BP-1 | SyncWorker Push/Pull Split | HIGH | ✅ COMPLETED | 2026-07-20 |
| BP-2 | Transaction Wrapper + FailedSyncQueue | HIGH | ✅ COMPLETED | 2026-07-20 |
| BP-3 | ViewModel Sync State Consolidation | HIGH | ✅ COMPLETED | 2026-07-20 |
| BP-4 | Hardcoded → Named Constants | HIGH | ✅ DONE | 2026-07-20 |
| BP-5 | Financial Calc Extraction (Web) | HIGH | ⏸ OBSOLETE | 2026-07-20 |
| L-1 | Shared KMP Domain Models | DEFERRED | 🔶 PARTIAL | 2026-07-20 |
| L-2 | Legacy Backend Retirement | LOW | ❌ NOT DONE | 2026-07-20 |
| L-3 | AdManager Subscription Guard | LOW | ❌ NOT DONE | 2026-07-20 |

---

## A. SQLDelight Migration — ✅ COMPLETED

**Status**: Fully implemented as of 2026-07-20. All local database access routes through SQLDelight generated DAOs.

### Verification Evidence

| Requirement | Result | Proof |
|------------|--------|-------|
| `.sq` schema file created | ✅ Done | `shared/src/commonMain/sqldelight/com/storebook/inventoryapp/shared/data/local/StoreBook.sq` — 400+ LOC covering items, sales, sale_items, udhaar, expenses, suppliers, purchases, purchase_items, item_batches, sync_state |
| FailedSyncQueue table added | ✅ Done | `failed_sync_queue` in `.sq` with full CRUD (enqueue, dequeue, retry_count increment, PERMANENT_FAILURE status) |
| Generated DAOs exist | ✅ Done | `shared/build/generated/sqldelight/code/StoreBookDatabase/commonMain/` — StoreBookQueries.kt, StoreBookDatabase.kt, + 20+ per-entity classes |
| LegacySyncHelper.kt deleted | ✅ Done | File does not exist anywhere in codebase |
| Room dependency removed | ✅ Done | No "room" references in `build.gradle.kts` or `libs.versions.toml` |
| SqliteDriver used everywhere | ✅ Done | `AndroidSqliteDriver(StoreBookDatabase.Schema, ...)` in SyncWorker, ExpiryCheckWorker, ViewModelFactory (3 files) |

### What Changed

**Before**: Raw `SQLiteOpenHelper` + manual rawQuery + ContentValues cursor extraction across LegacySyncHelper, SyncWorker, ExpiryCheckWorker. No type safety, empty `onCreate`/`onUpgrade` stubs.

**After**: Single `.sq` file defines ALL tables; SQLDelight generates type-safe Kotlin DAOs. `shared/` module produces `StoreBookDatabase`, `StoreBookQueries`, and per-entity classes (Items.kt, Sales.kt, Sale_items.kt, etc.).

### Effort Invested
Approximately 13-16 hours across schema definition, 4-file migration (LegacySyncHelper deleted, SyncWorker/ExpiryCheckWorker/ViewModelFactory rewritten), driver setup, and dependency consolidation.

---

## B. High-Priority Refactors

### BP-1: Extract SyncWorker Push/Pull into Separate Methods — ✅ COMPLETED

**Implementation verified 2026-07-20:** `SyncWorker.kt` fully split into isolated stages.

| Method | Purpose | LOC range |
|--------|---------|-----------|
| `doWork()` | Entry point orchestration | L25 |
| `performSync()` | Top-level coordinator with progress callback | L46 |
| `processRetries()` | Replays failed_sync_queue entries | L86 |
| `retryPushItem()` / `retryPushSale()` | Per-entity retry helpers | L119-138 |
| `push()` | Aggregate push orchestrator (returns Int count) | L139 |
| `pushItems/PushSales/PushSaleItems/PushUdhaars/...` | Entity-specific push via generated DAOs | L152-282 |
| `pull()` | Pull remote changes, returns tuple of counts + failure flag | L283+ |

All methods call typed SQLDelight DAOs instead of raw SQL strings. Zero manual cursor extraction remaining.

### BP-2: Transaction Wrapper + FailedSyncQueue — ✅ COMPLETED

**Implementation verified 2026-07-20:** All shared KMP repositories wrap multi-step writes in `database.transaction {}`.

| Repository | Transactions | Code Lines |
|-----------|-------------|------------|
| SalesRepository.kt | 5+ (insertSale, updateSyncState, atomicCheckout, convertQuotationToSale) | L31, L46, L61, L97, L157 |
| InventoryRepository.kt | 4+ (all CRUD wrapped — E02-S1) | L38, L53, L63, L91 |
| PurchaseRepository.kt | 2+ | L26, L36 |
| ExpenseRepository.kt | 1+ | L22 |
| BatchRepository.kt | 1+ | L27 |

**FailedSyncQueue**: Fully implemented in `.sq` with `enqueue_failed_sync`, `get_overdue_for_retry`, `update_failed_sync_retry_count`, `dequeue_failed_sync_by_id`, and `mark_permanent_failure`. Generated code at `build/generated/sqldelight/.../Failed_sync_queue.kt`.

### BP-3: Consolidate ViewModel Sync State Observation — ✅ COMPLETED

**Implementation verified 2026-07-20:**

A unified `SyncStatusViewModel` now serves as the **single source of truth** for sync state across all screens:

| File | What it does |
|------|-------------|
| `SyncStatusViewModel.kt` (NEW) | 150+ LOC — combines `NetworkMonitor` connectivity + `SyncRepository.getSyncState()` into single `StateFlow<UiSyncStatus>` hub; exposes `retrySync()` |
| `UiSyncStatus` data class | Unified DTO: `status`, `lastSyncAt`, `failedCount`, `isOnline` (+ computed `isSyncing`) |
| `DashboardViewModel.kt` (MODIFIED) | Removed all local sync polling loop, `refreshSyncStatus()`, WorkManager direct calls. Now delegates via `_syncSource` forwarder to `SyncStatusViewModel` |
| `AppViewModelFactory.kt` (MODIFIED) | Registers singleton `SyncStatusViewModel(context, syncRepository)`; injects into DashboardViewModel after construction |

**Before/After:**

```
BEFORE                          AFTER
------                          -----
DashboardVm.syncPoll → own      DashboardVm.uiSyncStatus → SyncStatusHub.syncState
AppConfigVm.NetworkMonitor →    (same NetworkMonitor instance via hub)
Fragmented retries              retrySync() delegates to WorkManager via hub
```

**Verification:**
- `SyncStatusViewModel.kt` exists under `ui/viewmodel/`
- DashboardScreen still uses `.status`, `.isSyncing`, `.failedCount` on `uiSyncStatus` — all present in new data class
- No compilation break: same property surface, different backing source

**Status**: ✅ COMPLETED. Sync observation is now centralized. Other ViewModels (MoreViewModel, etc.) can be wired up similarly as part of e01-s4 extension.

### BP-3 Execution Notes

Remaining work for e01-s4 expansion:
1. Wire MoreViewModel to observe `SyncStatusViewModel.syncState` for the "Last Sync" row in Settings
2. Wire navigation drawer footer to show live sync icon via `SyncStatusViewModel`
3. Consider migrating AppConfigViewModel out of manual AndroidViewmodel creation into AppViewModelFactory (would eliminate its separate NetworkMonitor instance)
```

### BP-4: Move Hardcoded Thresholds to Named Constants — ✅ DONE

**Implementation verified 2026-07-20:**

- **Android**: All magic numbers (`86_400_000`, `7_200_000`, photo size constants) removed. No matches found across `app/src/main/` via grep.
- **Web**: `web/src/lib/constants.ts` exists and exports named constants (currently: country codes array). Additional threshold values should route through this file as touched.

**Status**: ✅ DONE for Android. Web has skeleton `constants.ts`.

### BP-5: Extract Financial Calculations from Web Server Actions — ⏸ OBSOLETE (Design Shift)

**Current Reality (verified 2026-07-20):**

The web architecture shifted away from Express-style server actions performing financial calculations. Current `web/src/actions.ts` is minimal and focused on Firebase admin operations only (login, store/user management, staff creation). Financial data retrieval happens directly through **Firebase Data Connect GraphQL clients**.

| Original Concern | Current State | Verdict |
|------------------|--------------|---------|
| Udhaar balance in route handler | Not present — balance computed client-side via Data Connect queries | ✅ N/A |
| Sale total aggregation server-side | `web/src/lib/BillingEngine.ts` handles PDF invoice generation separately | ✅ Separated |
| Mixed calculation + fetch | Server actions are thin admin wrappers; actual CRUD goes through Data Connect SDK | ✅ Cleaned by design shift |

**No action needed.** Architectural migration to Firebase Data Connect eliminated the original problem. If financial calculations ever reappear in server handlers, reopen this task.

---

## C. Lower Priority / Deferred

### L-1: Shared KMP Domain Models — 🔶 PARTIAL

**Current Reality (verified 2026-07-20):**

8 domain repositories exist under `shared/src/commonMain/kotlin/...repository/`:
SalesRepository, InventoryRepository, PurchaseRepository, UdhaarRepository, ExpenseRepository, BatchRepository, SupplierRepository, SyncRepository.

These are fully typed and consume SQLDelight-generated DAOs on Android. However, **the web side does not yet consume shared KMP models** — the web uses its own DTOs from Firebase Data Connect TypeScript SDK responses. True cross-platform type sharing hasn't been wired end-to-end.

**Progress**: Foundation laid (repositories exist in shared/), but web integration pending.
**Defer to**: Phase 5 / v1.7.0+ when web needs typed KMP model consumption.
**Effort**: ~8-10 hours when the time comes.

### L-2: Legacy Express.js Backend Retirement — ❌ NOT DONE

**Current Reality (verified 2026-07-20):**

`backend/` directory still exists at root with node_modules and Express route definitions. Firebase Data Connect has fully replaced its functionality, but the directory hasn't been renamed or archived yet.

**Action**: Rename `backend/` → `backend-legacy/` + add README.md explaining supersession by Data Connect. Full deletion after 30 days post-v1.3.0 release.
**Effort**: ~15 min. **Scope**: Any time, non-blocking.

### L-3: AdManager Subscription Guard — ❌ NOT DONE

**Current Reality (verified 2026-07-20):**

`StoreBookApplication.kt` (L27) calls `MobileAds.initialize(this)` unconditionally at startup regardless of subscription tier. Two ad manager classes exist:
- `AppOpenAdManager.kt` (`ui/admanager/`)
- `InterstitialAdManager.kt` (`utils/`)

No check for paid user status occurs before SDK initialization. Paid users waste battery initializing the ad system on every cold start.

**Refactor needed**: Check subscription status BEFORE `MobileAds.initialize()`. If paid → skip entirely.
**Scope**: Deferred until e05 (subscription gating cycle).
**Effort**: ~1 hour when the time comes.

---

## D. Updated Execution Order & Dependencies

```
RP-A0 (SQLDelight migration) ✅ COMPLETED
    │ └─ Blocks removed. All remaining refactors can proceed.
    │
    ├── BP-1 (SyncWorker split)       ✅ COMPLETED
    │   │
    ├── BP-2 (transaction + queue)     ✅ COMPLETED
    │   │
    ├── BP-4 (constants extract)       ✅ DONE
    │   │
    ├── BP-5 (calc extraction web)     ⏸ OBSOLETE — no action needed
    │
    └── BP-3 (SyncStatusVM)            ✅ COMPLETED 2026-07-20

[Remaining work items]
L-1  (KMP models shared with web)      🔶 PARTIAL — defer to v1.7.0+
L-2  (legacy backend rename)           ❌ NOT DONE — any time, ~15 min
L-3  (ad init guard)                   ❌ NOT DONE — inline with e05-s1
```

### Remaining Effort Estimate

| Task | Estimated Hours | Timing |
|------|----------------|--------|
| L-2 (backend rename) | 0.25h | Any time |
| L-1 (KMP web integration) | 8-10h | v1.7.0+ |
| L-3 (ad init guard) | 1h | With e05-s1 |
| **Total** | **~9-11h** | Spread across cycles |

### Compliance Rule

> No code that mutates or queries local SQLite data may land using raw strings or Room DAOs. All access MUST route through SQLDelight-generated classes in the `shared/` module.

---

_Last verified against source: 2026-07-20 by map-codebase skill._
