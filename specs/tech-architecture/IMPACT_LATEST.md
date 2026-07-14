# Implementation Impact Analysis — StoreBook Epic Rollout

impact_plan_doc: IP1.0
last_updated: "2025-07-13"
epics_covered: [e01, e02, e03]
scope_targets: ["Sync reliability", "Inventory transaction safety", "Billing price audit"]

---

## A. Android Codebase Impact (E01 + E02)

### Existing Files Modified

| File | Change Type | Risk Level | Reason |
|------|------------|------------|--------|
| `workers/SyncWorker.kt` | Major refactor | HIGH | Rewire push→pull cycle, add FailedSyncQueue persistence |
| `services/SyncService.kt` | Major refactor | HIGH | Introduce RetryBackoffCalculator, circuit breaker for auth failures |
| `data/StoreBookDbHelper.kt` | Add table + methods | MEDIUM | New `FailedSyncQueue` table, batch query helpers |
| `ui/viewmodel/*ViewModel*.kt` (all ViewModels) | Add sync state observation | LOW-MEDIUM | Observe `SyncStatusViewModel` for UI feedback |
| `data/ItemsDao` or CRUD paths | Wrap in transactions | MEDIUM | `beginTransaction()` / `setTransactionSuccessful()` on addItem/updateItem/deleteItem |
| `services/SalesService.kt` (if exists) | Add price snapshot enforcement | LOW | Ensure sellPrice captured at sale time, not read from current Items table |

### New Files Created

| File | Purpose | Epic |
|------|---------|------|
| `workers/FailedSyncQueueWorker.kt` | Periodic retry processor for persisted FailedSyncQueue rows | e01-s2 |
| `services/RetryBackoffCalculator.kt` | Exponential backoff with jitter, max-retry cap enforcement | e01-s2 |
| `services/ConflictResolver.kt` | Last-Write-Wins comparator (updatedAt), merge logic | e01-s3 |
| `data/models/SyncFailureRecord.kt` | Data class for FailedSyncQueue rows: id, payload, retryCount, nextRetryAt, errorHash | e01-s2 |
| `ui/components/SyncStatusIndicator.kt` | Compose badge/banner showing sync state (synced / pending / failed) | e01-s4 |
| `services/ItemPhotoValidator.kt` | Size/type validation for photo uploads, path normalization | e02-s3 |

---

## B. Web Codebase Impact (E03 + E04 Prep)

### Existing Files Modified

| File | Change Type | Risk Level | Reason |
|------|------------|------------|--------|
| `web/src/app/(dashboard)/page.tsx` | Add audit summary widgets | LOW | Read-only financial diagnostic panels |
| `web/src/app/api/*sale-related*/route.ts` | Verify price snapshot on write | MEDIUM | Ensure SaleItem.write captures sellPrice from request body, not Items lookup |
| Server Actions for inventory | Verify transactional behavior in Data Connect calls | LOW | Check that multi-mutation batches are atomic (GQL already handles this, but audit) |
| Redux slices (`inventorySlice`, `udhaarSlice`) | Add computed selectors for balance totals | LOW | `selectUdhaarNetBalance(customerId)`, `selectSalePriceVariance()` |

### New Files Created

| File | Purpose | Epic |
|------|---------|------|
| `web/src/lib/financialAudit.ts` | Sale price variance calculator, udhaar balance verifier | e03-s1 |
| `web/src/components/admin/AuditDashboard.tsx` | Read-only panel showing variance reports, mismatch alerts | e03-s2 |
| `web/src/hooks/useAggregateCacheKeys.ts` | Helper to generate per-storeId cache key strings for ISR invalidation | e04 prep |

---

## C. Data Layer Impact

### SQLite Schema Changes (Android)

```sql
-- New table: FailedSyncQueue
CREATE TABLE IF NOT EXISTS FailedSyncQueue (
    id TEXT PRIMARY KEY,                    -- UUID of failed record
    entity_type TEXT NOT NULL,              -- 'Item' | 'SaleItem' | 'Udhaar'
    direction TEXT NOT NULL,                -- 'push' | 'pull'
    payload TEXT NOT NULL,                  -- JSON-serialized entity at time of failure
    retry_count INTEGER DEFAULT 0,
    next_retry_at INTEGER NOT NULL,         -- epoch millis
    error_message TEXT,
    store_id TEXT NOT NULL                  -- multi-tenancy guard
);

-- Index for efficient polling
CREATE INDEX IF NOT EXISTS idx_sync_queue_next_retry ON FailedSyncQueue(next_retry_at);
CREATE INDEX IF NOT EXISTS idx_sync_queue_store ON FailedSyncQueue(store_id);
```

**Migration strategy**: Android `onCreate` adds table unconditionally. For existing DBs (if any), implement version bump in `StoreBookDbHelper` with `onUpgrade` that checks for table existence before CREATE.

### Data Connect / PostgreSQL Changes

| Change | Purpose | Epic | Risk |
|--------|---------|------|------|
| Add `sync_timestamp_ms BIGINT` to items, sale_items, udhaar tables | Reliable Last-Write-Wins comparison | e01-s3 | LOW — additive column with default epoch 0 |
| Add `sell_price_snapshot NUMERIC(12,2)` to sale_items (verify exists) | Immutable price at time of sale | e03-s1 | LOW — should already exist per design spec |
| Create view `v_udhaar_balance_per_customer(storeId TEXT)` | On-demand balance computation for audit dashboard | e03-s2 | NONE — read-only view |

### Migration SQL

```sql
-- Run via Data Connect migration or pgAdmin directly
ALTER TABLE items ADD COLUMN IF NOT EXISTS sync_timestamp_ms BIGINT DEFAULT 0;
ALTER TABLE sale_items ADD COLUMN IF NOT EXISTS sync_timestamp_ms BIGINT DEFAULT 0;
ALTER TABLE udhaar ADD COLUMN IF NOT EXISTS sync_timestamp_ms BIGINT DEFAULT 0;

-- Verify sell_price_snapshot exists (idempotent check)
DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'sale_items' AND column_name = 'sell_price_snapshot'
  ) THEN
    ALTER TABLE sale_items ADD COLUMN sell_price_snapshot NUMERIC(12,2);
  END IF;
END $$;

CREATE OR REPLACE VIEW v_udhaar_balance_per_customer AS
SELECT 
    store_id,
    customer_id,
    SUM(CASE WHEN type = 'debit' THEN amount WHEN type = 'credit' THEN -amount ELSE 0 END) AS net_balance
FROM udhaar
WHERE is_deleted = FALSE
GROUP BY store_id, customer_id;
```

---

## D. Platform Risk Assessment

### High-Risk Changes (Must Have Rollback Plan)

| Change | Failure Mode | Mitigation |
|--------|-------------|------------|
| SyncWorker push→pull cycle rewrite | Workers crash-loop with 5xx errors → battery drain, data lost | Add max-retry cap at 7 attempts. After exhaustion, persist to FailedSyncQueue and STOP worker. User triggers manual retry from UI. |
| Transaction wrapping on item CRUD | Partial transaction failure leaves DB in inconsistent state | `setTransactionSuccessful()` only after ALL writes complete. Catch block calls explicit rollback → return failure Result. UI shows toast "Save failed — changes preserved locally." |
| FailedSyncQueue persistence with JSON payload | Large payloads (photos serialized inline) bloat SQLite | Payload must exclude blob photo data. Store only file path string in entity JSON, not byte arrays. Max row size target: 2KB. |

### Medium-Risk Changes

| Change | Failure Mode | Mitigation |
|--------|-------------|------------|
| Circuit breaker for Firebase auth | Over-aggressive trips → user appears logged out unnecessarily | Trip only after 3 consecutive token refresh failures within 60s window. Half-open after 5min cooldown. Manual reconnect button in UI. |
| ISR cache invalidation on web | Stale dashboard data after sync completes | Revalidate tag-based cache (`revalidateTag(storeId)`) after successful Data Connect mutation response from Android sync. |

### Low-Risk Changes (Safe to Deploy)

- New read-only views on PostgreSQL (`v_udhaar_balance_per_customer`)
- Audit dashboard panels on web (pure UI, no data mutations)
- Sync status UI indicators (observe existing state, no side effects)
- Computed selectors in Redux for financial summaries

---

## E. Testing Impact Summary

What changes require new or updated test coverage:

| Area | New Tests Needed | Type | Epic |
|------|-----------------|------|------|
| SyncWorker retry logic | ~12 unit tests (exponential backoff, max exhaustion, partial failure) | JUnit + Turbine | e01-s2 |
| FailedSyncQueue CRUD | ~8 unit tests (insert, query-by-nextRetry, delete-after-success) | JUnit | e01-s2 |
| ConflictResolver LWW | ~6 unit tests (local-wins, cloud-wins, same-timestamp tiebreak) | JUnit | e01-s3 |
| Transaction safety on items | ~8 integration tests (partial write rollback, concurrent inserts) | JUnit + Robolectric | e02-s1 |
| Sale price snapshot | ~6 unit tests (price frozen at sale time, post-sale item update doesn't affect history) | Jest/RTL | e03-s1 |
| Udhaar balance computation | ~4 unit tests (debit-credit math, soft-deleted records excluded) | Jest | e03-s1 |

**Total: ~44 new test cases** across Android and web platforms. See `TEST_PLAN_LATEST.md` for complete matrix per story.

---

## F. Rollback Playbook

| Scenario | Detection Signal | Rollback Action | Time Budget |
|----------|-----------------|-----------------|-------------|
| SyncWorker crashes on >10% of sync attempts within 24h | Crashlytics spike + user reports "never syncs" | Release hotfix that skips the push phase temporarily (pull-only mode). Users lose cloud backup for 1 cycle but local data is safe. | 48h from v1.3.0 release per `release-plan.yaml` rollback window |
| FailedSyncQueue fills >50MB on device (data leak) | Firebase Analytics custom event threshold | Disable queue persistence flag via remote config → fall back to in-memory retry only. Investigate payload bloat root cause. | Best-effort within 72h for non-crash degradation |
| Transaction wrapping causes 3x slower writes on low-end devices | Performance monitoring p95 write latency >3s | Add `@WorkerThread` annotation verification + batch writes to single transaction instead of per-item transactions. Profile with Android Studio CPU profiler. | Next patch release (≤1 week) |
