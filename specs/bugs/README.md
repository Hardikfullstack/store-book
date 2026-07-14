# P0 summary of audit findings

| Bug ID | Severity | Title | Component |
|--------|----------|-------|-----------|
| [BUG-01](./bug-01.yaml) | **P0 — Critical** | Non-atomic sale save allows partial stock deduction on crash | `SalesViewModel`, `InventoryRepository` |
| [BUG-02](./bug-sync-worker-silent-catch-all.yaml) | **P0 — Critical** | SyncWorker silent catch — errors discarded, no retry | `SyncWorker` |
| [BUG-03](./bug-sale-item-orphan-during-pull.yaml) | **P0 — Critical** | SaleItems inserted with parent_id=0 — orphan records created | `SyncRepository`, `SyncWorker` |
| [BUG-04](./bug-npe-hazards-double-bang-assertions.yaml) | **P0 — Critical** | 26 !! assertions on nullable state — unpredictable NPE crashes | `UdhaarScreen`, `InventoryScreen`, etc. |
| [BUG-05](./web-session-backdoor-note.yaml) | **P0 — Critical** | Backdoor comment + no CSRF protection on mutations | `actions.ts`, `session.ts` |
| [BUG-06](./bug-negative-stock-race-quotation.yaml) | **P0 — Critical** | Quotation conversion read-then-write — stock goes negative | `actions.ts` |

## ✅ P1 — High

| Bug ID | Severity | Title | Component |
|--------|----------|-------|-----------|
| [BUG-07](./bug-inventory-negative-quantity.yaml) | **P1 — High** | No guard against newQty < 0 — negative inventory | `InventoryRepository` |
| [BUG-08](./bug-udhaar-decimal-locale-crash.yaml) | **P1 — High** | Udhaar !! NPE crash on Indian locale separator | `UdhaarScreen` |
| [BUG-09](./bug-web-stale-dashboard.yaml) | **P1 — High** | Dashboard not invalidated after mutations — stale revenue | `actions.ts`, `DashboardClient.tsx` |
| [BUG-10](./bug-sync-worker-duplicate-sales.yaml) | **P1 — High** | Overlapping sync runs can push same sale — no idempotency key | `SyncRepository`, `SyncWorker` |
| [BUG-11](./bug-udhaar-balance-client-only.yaml) | **P1 — High** | Udhaar balance computed locally — two devices disagree | `UdhaarRepository`, `SyncWorker` |

## ⚠️ P2 — Medium

| Bug ID | Severity | Title | Component |
|--------|----------|-------|-----------|
| [BUG-12](./bug-sync-fake-success.yaml) | **P2 — Medium** | Sync status DONE when all pulls fail silently | `SyncWorker` |
| [BUG-13](./bug-web-as-any-casts.yaml) | **P2 — Medium** | 33 as any casts in prod TS — silent undefined on API change | `actions.ts`, session.ts, Sidebar.tsx` |
| [BUG-14](./bug-web-pdf-invoice-generator.yaml) | **P2 — Medium** | InvoicePdfGenerator NaN / negative totals when data corrupt | `InvoicePdfGenerator.ts` |
| [BUG-15](./bug-float-stock-drift.yaml) | **P2 — Medium** | REAL column + repeated deduction → IEEE 754 drift | `InventoryRepository`, `StoreBook.sq` |
| [BUG-16](./bug-web-duplicate-revalidatepath.yaml) | **P2 — Medium** | Multiple revalidatePath('/') calls per action — cache churn | `actions.ts` |
| [BUG-17](./bug-sales-history-items-always-empty.yaml) | **P2 — Medium** | getSalesByDateRange always maps items to emptyList() | `SalesViewModel` |
| [BUG-18](./bug-backup-progress-stuck-on-error.yaml) | **P2 — Medium** | Cloud backup progress stuck at partial % on failure | `MoreViewModel`, `BackupManager` |

## 📋 P3 — Low / Cleanup

| Bug ID | Severity | Title | Component |
|--------|----------|-------|-----------|
| [BUG-19](./bug-unused-columns-schema-bloat.yaml) | **P3 — Low** | 8+ columns never queried by application code | `StoreBook.sq` |
| [BUG-20](./bug-duplicate-sync-pull-implementations.yaml) | **P3 — Low** | Two upsert variants per entity type — risk of divergence | `SyncRepository` |
| [BUG-21](./bug-missing-input-validation-forms.yaml) | **P3 — Low** | No max-length on text fields, no email RFC validation | Multiple forms |
| [BUG-22](./web-sidebar-permission-type-safety-gap.yaml) | **P3 — Low** | Dynamic (perms as any)[permKey] — typos bypass compile safety | `Sidebar.tsx` |
