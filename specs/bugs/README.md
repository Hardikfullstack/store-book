# Bugs — StoreBook

This directory tracks identified bugs with IDs matching filename stems. `registry.yaml` is the single source of truth; this README mirrors its contents for quick reference.

Last synced with registry: 2026-10-12

## Status Summary

| Severity | Count |
|----------|-------|
| P0 (Critical) | 13 resolved, 0 open |
| P1 (High) | 18 resolved, 1 open |
| P2 (Medium) | 5 resolved, 0 open |
| **Total** | **35 resolved, 1 open (36 total)** |

## P0 — Critical

| Bug ID | Title | Status | File |
|--------|-------|--------|------|
| BUG-01 | Non-atomic sale save allows partial stock deduction on crash | Resolved | [bug-01.yaml](./bug-01.yaml) |
| BUG-02 | SyncWorker silent catch — errors discarded, no retry | Resolved | [bug-sync-worker-silent-catch-all.yaml](./bug-sync-worker-silent-catch-all.yaml) |
| BUG-03 | SaleItems inserted with parent_id=0 on cloud-ID resolution failure | Resolved | [bug-sale-item-orphan-during-pull.yaml](./bug-sale-item-orphan-during-pull.yaml) |
| BUG-04 | 26 !! assertions on nullable state — NPE crashes | Resolved | [bug-npe-hazards-double-bang-assertions.yaml](./bug-npe-hazards-double-bang-assertions.yaml) |
| BUG-05 | Backdoor comment + no CSRF protection on web mutations | Resolved | [web-session-backdoor-note.yaml](./web-session-backdoor-note.yaml) |
| BUG-06 | Quotation conversion read-then-write — stock goes negative | Resolved | [bug-negative-stock-race-quotation.yaml](./bug-negative-stock-race-quotation.yaml) |
| post-sync-stale-ui | Data synced after fresh login not visible until app reopened | Resolved | [bug-post-sync-stale-ui.yaml](./bug-post-sync-stale-ui.yaml) |
| BUG-17 | `storeRes.data.store!!` NPE crash when store field is null during login | Resolved | [bug-A1-store-null-ptr-login.yaml](./bug-A1-store-null-ptr-login.yaml) |
| BUG-19 | `mutableStateOf` cartItems/lastSaleId written from IO dispatcher | Resolved | [bug-A3-vm-thread-safety.yaml](./bug-A3-vm-thread-safety.yaml) |
| BUG-22 | Sequential awaits in web checkout — partial commits on failure | Resolved | [bug-W1-web-partial-checkout.yaml](./bug-W1-web-partial-checkout.yaml) |
| BUG-27 | App crashes when clicking 'View All' on Dashboard due to integer overflow | Resolved | [bug-A7-sales-history-view-all-crash.yaml](./bug-A7-sales-history-view-all-crash.yaml) |
| BUG-29 | Android: More tab crashes on emulator due to main-thread DB queries and unauthenticated DataConnect call | Resolved | [bug-A9-more-screen-init-crash.yaml](./bug-A9-more-screen-init-crash.yaml) |
| BUG-30 | Android: OTP login crashes on release build due to R8 stripping za.g2 reflection field | Resolved | [bug-A10-otp-login-release-crash.yaml](./bug-A10-otp-login-release-crash.yaml) |

## P1 — High

| Bug ID | Title | Status | File |
|--------|-------|--------|------|
| BUG-07 | No guard against newQty < 0 — negative inventory | Resolved | [bug-inventory-negative-quantity.yaml](./bug-inventory-negative-quantity.yaml) |
| BUG-08 | Udhaar amount !! NPE crash on Indian locale format | Resolved | [bug-udhaar-decimal-locale-crash.yaml](./bug-udhaar-decimal-locale-crash.yaml) |
| BUG-09 | Dashboard not invalidated after mutations — stale revenue | Resolved | [bug-web-stale-dashboard.yaml](./bug-web-stale-dashboard.yaml) |
| BUG-10 | Udhaar balance computed locally — two devices disagree | Resolved | [bug-udhaar-balance-client-only.yaml](./bug-udhaar-balance-client-only.yaml) |
| BUG-11 | Inventory restock quantity increases incorrectly (non-atomic addition, duplicate click) | Resolved | [bug-restock-quantity-incorrect.yaml](./bug-restock-quantity-incorrect.yaml) |
| BUG-12 | Entered Tax Rate not persisted to database on create/update | Resolved | [bug-tax-rate-not-saved.yaml](./bug-tax-rate-not-saved.yaml) |
| BUG-13 | Inventory stock increases instead of decreasing when a sale is created | Resolved | [bug-sale-increases-stock.yaml](./bug-sale-increases-stock.yaml) |
| BUG-18 | SalesAnalytics custom date range filter chip NPE risk | Resolved | [bug-A2-sales-analytics-custom-date-npe.yaml](./bug-A2-sales-analytics-custom-date-npe.yaml) |
| BUG-20 | SyncWorker pull hardcodes taxRate=0.0 and hsnCode=null | Resolved | [bug-A4-pull-saleitem-tax-hsn.yaml](./bug-A4-pull-saleitem-tax-hsn.yaml) |
| BUG-21 | Udhaar insert outside transaction in checkout | Resolved | [bug-A5-udhaar-insert-outside-txn.yaml](./bug-A5-udhaar-insert-outside-txn.yaml) |
| BUG-23 | Web POS writes absolute stale stock to server | Resolved | [bug-W2-web-stale-inventory.yaml](./bug-W2-web-stale-inventory.yaml) |
| BUG-25 | Web checkout error path leaves committed sale header | Resolved | [bug-W4-web-checkout-duplicate.yaml](./bug-W4-web-checkout-duplicate.yaml) |
| BUG-26 | Inventory CSV export/import includes ID column | Resolved | [bug-A6-csv-import-export-id-column.yaml](./bug-A6-csv-import-export-id-column.yaml) |
| BUG-28 | Sales Analytics (Product, Customer, Timeline) loads from local SQL instead of cloud API | Resolved | [bug-A8-sales-analytics-api-data.yaml](./bug-A8-sales-analytics-api-data.yaml) |
| BUG-31 | Web: Clearing numeric form inputs sets NaN in state triggering React warning | Resolved | [bug-W5-nan-numeric-inputs.yaml](./bug-W5-nan-numeric-inputs.yaml) |
| BUG-32 | Web POS: Cart quantity input is read-only and lacks stock limit validation | **Open** | [bug-W6-pos-cart-editable-quantity-stock-limit.yaml](./bug-W6-pos-cart-editable-quantity-stock-limit.yaml) |
| BUG-34 | Android: Item batch number and expiry date wiped on edit and omitted from push | Resolved | [bug-A6-item-batch-expiry-tracking-sync.yaml](./bug-A6-item-batch-expiry-tracking-sync.yaml) |
| BUG-35 | Android: Local SQLite numeric ID vs Cloud DataConnect UUID mismatch causing type conflicts | Resolved | [bug-A11-numeric-id-instead-of-uuid.yaml](./bug-A11-numeric-id-instead-of-uuid.yaml) |

## P2 — Medium

| Bug ID | Title | Status | File |
|--------|-------|--------|------|
| BUG-14 | Estimate created from sale not added to Quotation list | Resolved | [bug-estimate-not-saved.yaml](./bug-estimate-not-saved.yaml) |
| BUG-15 | Udhaar created from sale not added to Udhaar list | Resolved | [bug-udhaar-not-saved.yaml](./bug-udhaar-not-saved.yaml) |
| BUG-16 | HSN code misused as barcode — scanner matches tax classification instead of product ID | Resolved | [bug-16-hsn-barcode-separation.yaml](./bug-16-hsn-barcode-separation.yaml) |
| BUG-24 | Explicit `any` types in web/src violate strict-mode | Resolved | [bug-W3-any-types-strict-mode.yaml](./bug-W3-any-types-strict-mode.yaml) |
| BUG-33 | Web: Item batch/expiry missing from GetActiveItems query and barcode under advanced | Resolved | [bug-W7-item-batch-expiry-barcode-placement.yaml](./bug-W7-item-batch-expiry-barcode-placement.yaml) |
