# Test Plan — StoreBook Epic Rollout

test_plan_doc: TP1.0
last_updated: "2026-07-13"
epics_covered: [e01, e02, e03]
test_framework_kotlin: JUnit 4 + Mockito + Turbine (Flow testing)
test_framework_ts: Jest + React Testing Library + MSW (API mocking)

---

## A. Unit Tests — Kotlin (Android + KMP shared module)

### A1. SyncWorker Retry Logic (e01-s2)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| e01-u01 | First retry returns correct delay | retryCount=0 | 5_000ms | Unit |
| e01-u02 | Second retry doubles delay | retryCount=1 | 10_000ms | Unit |
| e01-u03 | Max retries caps delay at maxDelayMs | retryCount=10 | 300_000ms | Unit |
| e01-u04 | All retries exhausted → status=MAX_RETRIES_EXCEEDED | FailedSyncQueue row with retryCount=3 | Status updated, notification sent | Unit |
| e01-u05 | FailedSyncQueue insert succeeds after mutation failure | Mocked network timeout on SyncWorker.pushSingle() | Queue row created with correct payload+reasonCode | Unit |

### A2. ConflictResolver (e01-s3)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| e01-u06 | Remote newer → accept remote | local.updatedAt=100, remote.updatedAt=200 | Item updated to remote version, local synced flag set | Unit |
| e01-u07 | Local same or newer → keep local | local.updatedAt=200, remote.updatedAt=150 | Local unchanged, no overwrite | Unit |
| e01-u08 | Sale with duplicate ID skipped | saleId exists locally AND remotely | No insert, log.info("Sale already synced") | Unit |
| e01-u09 | Empty pull result → no crash | Pull returns empty list | SyncState.lastSync updated, no item changes | Unit |

### A3. Inventory Transaction Safety (e02-s1)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| e02-u10 | addItem with valid data → success | Item(name="Rice", price=50, qty=100) | rowId > 0, DB count increases by 1 | Unit/Integration |
| e02-u11 | addItem triggers exception → rollback | Mock insertItem() throws SQLiteException | No new row, transaction ended cleanly, no leak | Unit |
| e02-u12 | deleteItem sets isDeleted=true (soft-delete) | itemId exists | is_deleted = true in DB, NOT hard DELETE | Unit/Integration |
| e02-u13 | updateItem partial fields → only changed columns written | name updated, price unchanged | Only name column modified, updatedAt bumped | Unit |

### A4. Low-Stock Worker Branch (e02-s2)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| e02-u14 | Item below threshold → alert fired | qty=5, threshold=10 | Notification sent, low_stock_alert_sent=true | Unit |
| e02-u15 | Item already alerted → no duplicate | alert_sent=true | No notification, query skips record | Unit |
| e02-u16 | Item restocked above threshold → flag reset | qty goes 5→15 | low_stock_alert_sent=false, ready for future alerts | Unit |

### A5. SaleItem Price Snapshot (e03-s1)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| e03-u17 | SaleItem stores price at billing time | Item sellPrice=100 at sale time | SaleItem.sellPrice = 100 even if Item later changes to 120 | Unit/Integration |
| e03-u18 | Changing Item price doesn't retroactively alter SaleItems | Update item after sale exists | Original SaleItem unchanged, audit query returns "OK" | Integration |

### A6. Udhaar Balance Computation (e03-s2)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| e03-u19 | Single outstanding → balance = amount | totalOutstanding=500, paid=0 | currentBalance=500 | Unit |
| e03-u20 | Partial payment reduces balance | outstanding=1000, paid=400 | currentBalance=600 | Unit |
| e03-u21 | Full payment → zero balance | outstanding=800, paid=800 | currentBalance=0 | Unit |
| e03-u22 | Over-payment → negative balance (credit to customer) | outstanding=500, paid=600 | currentBalance=-100 (flagged for manual review) | Unit |

---

## B. Integration Tests — Android (SQLite level)

### B1. Database Migration Safety (all epics)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| int-b01 | Add FailedSyncQueue table → no migration crash | New SQLiteOpenHelper version increment | DB schema updated, existing data preserved | Integration |
| int-b02 | Soft-delete cascade: deleted item doesn't break sale history | Delete item that appears in past sales | SaleItems still queryable, summary unaffected | Integration |
| int-b03 | SyncWorker end-to-end: push 10 items, verify all synced | Populate local DB with 10 new items | All 10 appear in remote queries after worker runs | Integration (mock Firebase) |

### B2. Quotation → Sale Conversion Transaction (e03-s4)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| int-b04 | Convert valid quotation with 5 line items | Quotation exists, 5 lines | Sale created with same 5 lines, quotation marked converted | Integration |
| int-b05 | Convert empty quotation → failure | Quotation has 0 lines | Exception thrown, no sale created, quotation unchanged | Integration |
| int-b06 | Double-convert protection | Already converted quotation | Second call returns error, no duplicate sale | Integration |

---

## C. Web Tests — TypeScript/Next.js (Jest + RTL)

### C1. Dashboard Aggregates (e03-s3)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| web-c01 | getDailySales returns correct sum per day | Mock DB with 3 sales on same date | Single row with summed total | Unit (Server Action) |
| web-c02 | Empty date range → empty array, not crash | startDate > endDate | [] returned, status 200 | Unit |
| web-c03 | Profit calculation matches manual sum | 5 sale records with known buy/sell prices | Calculated profit = Σ(sell - buy), within ₹0.01 tolerance | Unit |

### C2. Role-Based Permission Checks (e04-s2)

| Test ID | Scenario | Input | Expected Output | Type |
|---------|----------|-------|-----------------|------|
| web-c04 | Cashier accesses admin route → 403 | Session role=cashier, GET /admin/users | Response 403 or redirect to dashboard | Integration (MSW) |
| web-c05 | Manager can view analytics but not users | Session role=manager | Analytics loads, Admin hidden/disabled | Integration |
| web-c06 | Owner accesses all routes → success | Session role=owner | All pages return 200 | Integration |

---

## D. Test Infrastructure

### Code Coverage Targets

| Module | Minimum Branch Coverage | Rationale |
|--------|------------------------|-----------|
| SyncWorker + ConflictResolver | 90% | High risk — sync corruption directly causes data loss |
| BillingEngine + SaleItem logic | 95% | Financial correctness must be near-perfect |
| Inventory CRUD wrappers | 85% | Standard CRUD, transaction boundary testing covers most |
| Web Server Actions (aggregates) | 80% | Read-only analytics, failure modes are cache misses not data corruptions |

### CI Pipeline (GitHub Actions)

```yaml
# .github/workflows/test.yml (target structure)
jobs:
  kotlin_test:
    runs-on: ubuntu-latest
    steps:
      - run: ./gradlew :app:testDebugUnitTest
      - run: ./gradlew :shared:allTests

  web_test:
    runs-on: ubuntu-latest
    steps:
      - run: cd web && npm ci && npm test
      - run: cd web && npm run lint

  coverage_gate:
    needs: [kotlin_test, web_test]
    if: always()
    steps:
      - name: Fail if any upstream job failed
        run: |
          # enforce green pipeline before merge to master
```

### Manual QA Checklist (per release)

- [ ] Android app cold start → Dashboard loads within 3s
- [ ] Go offline (airplane mode) → create sale, exit app
- [ ] Restore WiFi → wait for sync → verify sale appears on web dashboard
- [ ] Create Udhaar entry → make partial payment → verify balance updates correctly
- [ ] Delete an item that appears in historical sales → sale summary still shows it
- [ ] Web login as cashier → verify admin links hidden
