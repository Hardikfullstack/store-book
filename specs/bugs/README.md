# Bugs — StoreBook

This directory tracks identified bugs with IDs matching filename stems. `registry.yaml` is the single source of truth; this README mirrors its contents for quick reference.

Last synced with registry: 2026-07-28

## Status Summary

| Severity | Count |
|----------|-------|
| P0 (Critical) | 7 resolved |
| P1 (High) | 7 resolved |
| P2 (Medium) | 3 resolved |
| **Total** | **17 resolved, 0 open** |

## P0 — Critical

| Bug ID | Title | File |
|--------|-------|------|
| BUG-01 | Non-atomic sale save allows partial stock deduction on crash | [bug-01.yaml](./bug-01.yaml) |
| BUG-02 | SyncWorker silent catch — errors discarded, no retry | [bug-sync-worker-silent-catch-all.yaml](./bug-sync-worker-silent-catch-all.yaml) |
| BUG-03 | SaleItems inserted with parent_id=0 on cloud-ID resolution failure | [bug-sale-item-orphan-during-pull.yaml](./bug-sale-item-orphan-during-pull.yaml) |
| BUG-04 | 26 !! assertions on nullable state — NPE crashes | [bug-npe-hazards-double-bang-assertions.yaml](./bug-npe-hazards-double-bang-assertions.yaml) |
| BUG-05 | Backdoor comment + no CSRF protection on web mutations | [web-session-backdoor-note.yaml](./web-session-backdoor-note.yaml) |
| BUG-06 | Quotation conversion read-then-write — stock goes negative | [bug-negative-stock-race-quotation.yaml](./bug-negative-stock-race-quotation.yaml) |
| post-sync-stale-ui | Data synced after fresh login not visible until app reopened | [bug-post-sync-stale-ui.yaml](./bug-post-sync-stale-ui.yaml) |

## P1 — High

| Bug ID | Title | File |
|--------|-------|------|
| BUG-07 | No guard against newQty < 0 — negative inventory | [bug-inventory-negative-quantity.yaml](./bug-inventory-negative-quantity.yaml) |
| BUG-08 | Udhaar amount !! NPE crash on Indian locale format | [bug-udhaar-decimal-locale-crash.yaml](./bug-udhaar-decimal-locale-crash.yaml) |
| BUG-09 | Dashboard not invalidated after mutations — stale revenue | [bug-web-stale-dashboard.yaml](./bug-web-stale-dashboard.yaml) |
| BUG-10 | Udhaar balance computed locally — two devices disagree | [bug-udhaar-balance-client-only.yaml](./bug-udhaar-balance-client-only.yaml) |
| BUG-11 | Inventory restock quantity increases incorrectly (non-atomic addition, duplicate click) | [bug-restock-quantity-incorrect.yaml](./bug-restock-quantity-incorrect.yaml) |
| BUG-12 | Entered Tax Rate not persisted to database on create/update | [bug-tax-rate-not-saved.yaml](./bug-tax-rate-not-saved.yaml) |
| BUG-13 | Inventory stock increases instead of decreasing when a sale is created | [bug-sale-increases-stock.yaml](./bug-sale-increases-stock.yaml) |

## P2 — Medium

| Bug ID | Title | File |
|--------|-------|------|
| BUG-14 | Estimate created from sale not added to Quotation list | [bug-estimate-not-saved.yaml](./bug-estimate-not-saved.yaml) |
| BUG-15 | Udhaar created from sale not added to Udhaar list | [bug-udhaar-not-saved.yaml](./bug-udhaar-not-saved.yaml) |
| BUG-16 | HSN code misused as barcode — scanner matches tax classification instead of product ID | [bug-16-hsn-barcode-separation.yaml](./bug-16-hsn-barcode-separation.yaml) |
