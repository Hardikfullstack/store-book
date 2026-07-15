# Design Plan — StoreBook Epic Rollout

plan_doc: DP1.0
last_updated: "2026-07-13"
epics_planned_for: [e01, e02, e03]
status: draft

---

## A. Sync Worker Redesign (e01)

### Current Architecture

```
┌──────────────────────┐
│  Android Local SQLite │◄── StoreBookDbHelper (Room/SqlDelight)
│  + SyncState table   │
└─────────┬────────────┘
          │ PeriodicWorkRequest (hourly or manual trigger)
          ▼
┌──────────────────────┐
│    SyncWorker        │── Push phase: iterate unsynced items/sales/udhaar
│  (408 LOC)           │   via DataConnectConnector.mutations()
└─────────┬────────────┘          ─── Pull phase: high-water-mark queries
          │ Firebase Data Connect
          ▼
┌──────────────────────┐
│  Cloud SQL PostgreSQL │── schema.gql @table definitions, @auth(level: USER)
└──────────────────────┘
```

### Design Changes

**1. Retry Queue (new data structure)**

- Add `FailedSyncQueue` table in local SQLite alongside existing tables
- Columns: `id`, `mutationPayload`, `originalMutationType` (item/sale/udhaar), `retryCount`,
  `nextRetryAt` (epoch ms), `reasonCode` (TIMEOUT, AUTH_FAILED, NETWORK_ERROR), `status` (PENDING, MAX_RETRIES_EXCEEDED)
- SyncWorker post-push phase: scan failed items → insert into queue with baseDelay=5000ms

**2. Exponential Backoff Calculator (new class)**

```kotlin
class RetryBackoffCalculator(
    val maxRetries: Int = 3,
    val baseDelayMs: Long = 5_000,
    val maxDelayMs: Long = 300_000
) {
    fun nextDelayAttempt(retryCount: Int): Long {
        val exponential = baseDelayMs * (2L).pow(retryCount)
        return minOf(exponential coalesce maxDelayMs, maxDelayMs)
    }
}
```

**3. Conflict Resolver (new strategy class)**

- Pull phase result comparison logic in new `ConflictResolver` class:
  - Items: compare local.updatedAt vs remote.updatedAt → last-write-wins
  - Sales: append-only check via unique saleId — if saleId exists locally AND remotely, skip
  - Udhaar: same as items (update-within-tenant scope)

**4. Sync Status ViewModel (new UI component)**

- New `SyncStatusViewModel` extending ViewModel
- State object: `data class SyncUiState(isActing: Boolean, lastSyncEpoch: Long, failureCount: Int)`
- Exposed via Flow<SyncUiState> to DashboardScreen → Compose `when(state.isActing) {...}`
- Manual retry: `LaunchedEffect` with button onClick = `viewModel.triggerManualSync()`

---

## B. Inventory Transaction Safety (e02)

### Current Architecture

```kotlin
// Current: individual inserts without transaction boundary
fun addItem(item: Item): Long {
    return dbHelper.insertItem(item.name, item.price, ...)  // single insert
}
```

### Design Changes

**1. Transaction Wrapper Pattern**

Replace bare inserts with `db.beginTransaction()` blocks:

```kotlin
fun addItemSafely(item: Item): Result<Long> = runCatching {
    db.beginTransaction()
    val rowId = dbHelper.insertItem(...)
    // Also update SyncState.lastSync for items if needed
    db.setTransactionSuccessful()
    return@runCatching Result.success(rowId)
}.onFailure {
    db.endTransaction()  // rollback on exception
} finally {
    if (db.inTransaction()) db.endTransaction()
}
```

Applied to: `addItem`, `updateItem`, `deleteItem` (soft-delete via isDeleted=true).

**2. Low-Stock Alert Integration into ExpiryCheckWorker**

Current worker only checks item expiry dates. Add query branch:

```sql
SELECT * FROM items
WHERE quantity < low_stock_threshold
  AND low_stock_alert_sent = false
  AND store_id = ?
```

For each result: send Android Notification via `NotificationCompat.Builder`, update flag `low_stock_alert_sent = true`. Reset flag when item is re-stocked above threshold.

**3. Photo Path Validation**

New utility `ItemPhotoValidator`:
- Before save: `File(photoPath).exists() && file.length() < 5 * 1024 * 1024`
- Before display: try/catch `BitmapFactory.decodeFile()` → on failure, return default drawable `R.drawable.ic_item_placeholder`

---

## C. Financial Accuracy Layer (e03)

### Current Architecture

SaleItems store `sellPrice` at time of sale — need to verify this snapshot is preserved when the
underlying Item price changes later. Currently no explicit test for this scenario.

### Design Changes

**1. SaleItem Price Snapshot Verification Query (audit tool)**

Add diagnostic query in web dashboard Admin panel:

```sql
SELECT si.id, si.item_id, si.sell_price AS sale_snapshot_price, i.current_sell_price,
       CASE WHEN si.sell_price != i.current_sell_price THEN 'OK' ELSE 'MISMATCH' END as status
FROM sale_items si
JOIN items i ON si.item_id = i.id
WHERE i.is_deleted = false
ORDER BY si.created_at DESC LIMIT 50;
```

This doesn't fix data — it surfaces anomalies for manual review.

**2. Udhaar Running Balance Computed Column**

Add computed field on Udhaar detail screen (not new DB column):

```kotlin
data class UdhaarBalance(
    val totalOutstanding: Double,
    val totalPaid: Double,
    val currentBalance: Double  // = totalOutstanding - totalPaid
)
```

Recomputed from raw records each time screen loads. No denormalized column needed for MVP — keep single source of truth.

**3. Daily/Monthly Aggregate Queries (Server Actions)**

New Next.js Server Actions in `web/src/app/api/`:

- `getDailySales(startDate, endDate)` → `SELECT DATE(created_at), SUM(total) FROM sales GROUP BY DATE`
- `getDailyExpenses(startDate, endDate)` → same pattern on expenses table
- Cached with Vercel ISR (revalidate: 300s = 5 min TTL per cache key)

**4. Quotation → Sale Conversion Transaction**

```kotlin
fun convertQuotationToSale(qId: Long): Result<Long> {
    db.beginTransaction()
    val lines = getQuotationLines(qId)
    if (lines.isEmpty()) return Result.failure(EmptyQuotationException())
    val saleId = createSale(lines.map { SaleItem(...) })
    markQuotationAsConverted(qId)
    db.setTransactionSuccessful()
    return Result.success(saleId)
} finally { ... }
```

---

## D. Platform Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Conflict strategy | Last-write-wins (updatedAt) | Simplest for single-user-per-store scenario. No operational transform needed yet. |
| Retry persistence | Local SQLite queue table | Offline resilience — survives app kill, device restart |
| Photo storage | File path string in DB + filesystem | Already established pattern. Cloud photo upload deferred to later epic. |
| Udhaar balance | Computed on demand | No denormalization risk. Query cost is negligible for <10k udhaar records per store. |
| Aggregate caching | Vercel ISR (5min) | Sufficient freshness for dashboard, avoids real-time WebSocket complexity. |
| **Local DB driver (HARD REQ)** | **CashApp SQLDelight** | **All local SQLite access MUST use SQLDelight `.sq` files — no raw `SQLiteOpenHelper`, no Room DAO strings, no hand-written SQL. Generates compile-validated Kotlin types. Deploys via RP-A0 before any epic implementation.** |
