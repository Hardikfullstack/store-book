# 05 — Business Logic & Validation

## GST Tax Engine (`BillingEngine.kt`)

### Overview
The `BillingEngine` is the authoritative tax calculation module for all sales. It implements India's Goods and Services Tax (GST) framework with BigDecimal precision arithmetic (4 decimal intermediate, 2 decimal final rounding, HALF_UP).

**Source**: `app/src/main/java/com/storebook/inventoryapp/data/billing/BillingEngine.kt`

### Tax Type Determination

```
If businessGstin AND customerGstin both have valid state codes AND state codes differ:
  → INTERSTATE (IGST only)
Else:
  → INTRASTATE (CGST + SGST split equally)
```

State code = first 2 characters of GSTIN.

### Calculation Flow

1. **Subtotal** = Σ(sellPrice × quantity) for all cart items.
2. **Actual discount** = min(totalDiscount, subtotal) — prevents negative totals.
3. **Net taxable amount** = subtotal - actualDiscount.
4. For each cart item:
   - `itemGross` = sellPrice × quantity
   - `itemDiscountRatio` = itemGross / subtotal (proportional)
   - `itemDiscount` = actualDiscount × itemDiscountRatio
   - `itemNetTaxable` = itemGross - itemDiscount (clamped ≥ 0)
   - If INTRASTATE: `CGST = itemNetTaxable × (taxRate/2) / 100`, `SGST = same`
   - If INTERSTATE: `IGST = itemNetTaxable × taxRate / 100`
5. **Grand total** = netTaxableAmount + totalCGST + totalSGST + totalIGST.

### Important: Tax is Applied Post-Discount
Discount is distributed proportionally across items **before** tax is calculated. This prevents rounding errors that would occur if tax were calculated first.

### Cross-Platform Parity
**Web**: The web app does NOT have a BillingEngine implementation. Tax calculations are NOT performed on the web dashboard. The web only displays pre-calculated sale totals from the database.

> **⚠ DIVERGENCE**: Tax logic exists only in Android. If sales are ever created from the web (currently they are not for real transactions), taxes will not be applied.

---

## Synchronization Engine (`SyncWorker.kt`)

### Architecture: "Ping and Delta"

**Source**: `app/src/main/java/com/storebook/inventoryapp/data/sync/SyncWorker.kt`

### Phase 1: Push (Local → Cloud)

For each entity type (Items, Sales, SaleItems, Udhaar, Expenses, Suppliers, Purchases, PurchaseItems, ItemBatches):
1. Query local SQLite for `WHERE is_synced = 0`.
2. For each unsynced record:
   - Call the corresponding Data Connect `Sync*` mutation.
   - If `cloud_id` is null, generate a new UUID.
   - On success: update local row with `is_synced = 1` and the returned `cloud_id`.
3. Handle errors per-record (skip failures, continue batch).

### Phase 2: Pull (Cloud → Local)

For each entity type:
1. Read `last_sync_timestamp_$storeId` from EncryptedSharedPreferences.
2. Call the corresponding `Sync*` Data Connect query with `lastSync` parameter.
3. For each returned record:
   - Check if a local row exists with matching `cloud_id`.
   - If exists: UPDATE local row (merge cloud data).
   - If not: INSERT new local row with `cloud_id` set, `is_synced = 1`.
4. Update `last_sync_timestamp_$storeId` to `max(updatedAt)` from response.

### Phase 3: Ping

After successful sync, write timestamp to Firebase RTDB:
```
store_updates/$storeId/last_update = System.currentTimeMillis()
```

Other clients (including web) listen to this path and trigger their own data refresh.

### Triggers

| Trigger | Source | Method |
|---|---|---|
| Post-login | AuthScreen | `WorkManager.enqueueUniqueWork()` |
| Manual sync | Dashboard pull-to-refresh | Direct `SyncWorker.performSync()` |
| Background periodic | WorkManager | Periodic constraint-based scheduling |
| RTDB ping received | DashboardScreen | Listener triggers sync |

### Conflict Resolution
**Last-writer-wins** based on `updated_at` timestamp. No merge logic — the latest mutation overwrites.

### Error Handling
- Network failures: WorkManager retries with exponential backoff.
- Individual record failures: Logged and skipped, other records continue.
- `CancellationException`: Always re-thrown (cooperative coroutine cancellation).

---

## Stock Management

### Stock Deduction (Sale)
**Source**: `StoreBookRepository.recordSale()` (lines 167-298)

1. Within a single SQLite transaction:
   - Skip stock deduction for `ESTIMATE` type sales.
   - Skip stock deduction for dummy items (`id = 0`, used in Quick Cash Sale).
   - For real items: read `currentQty`, verify `currentQty >= cartItem.quantity`.
   - If insufficient: throw `"Stock oversell error"` → entire transaction rolls back.
   - Otherwise: set `newQty = currentQty - cartItem.quantity`.

### Stock Restoration (Undo Sale)
**Source**: `StoreBookRepository.undoSale()` (lines 300-384)

1. Fetch sale items.
2. Check if sale was `ESTIMATE` — if so, skip stock restoration.
3. For real sales: `UPDATE items SET quantity = quantity + qty` for each item.
4. Soft-delete associated udhaar entries (matched by `notes LIKE "Sale bill #$saleId%"`).
5. Soft-delete sale + sale_items.

### Restock
**Source**: `StoreBookRepository.restockItem()` (lines 877-946)

1. Fetch current item.
2. `newQty = currentItem.quantity + quantityToAdd`.
3. Update item's `buyPrice` to the new `costPrice`.
4. Insert expense record of type `RESTOCK` with `amount = costPrice × quantityToAdd`.
5. All within single transaction.

### Purchase Stock Update
**Source**: `StoreBookRepository.insertPurchase()` (lines 1159-1205)

1. Insert purchase header.
2. For each purchase item: insert `purchase_items` row + update item's `quantity` and `buyPrice`.
3. All within single transaction.

---

## Udhaar (Credit) Logic

### Auto-Credit on Sale
When `paymentMode == "Udhaar"` and `customerName` is not blank and `type != "ESTIMATE"`:
- Auto-insert `UdhaarEntry(type="CREDIT", amount=total, notes="Sale bill #$saleId")`.

### Balance Computation
```sql
SELECT customer_name,
  SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END) as balance,
  MAX(timestamp) as last_time
FROM udhaar WHERE is_deleted = 0
GROUP BY customer_name
```
- **Positive balance** = customer owes store (Due).
- **Negative balance** = store owes customer (Advance).

### Customer Name Standardization
`StoreBookRepository.standardizeCustomerNames()` normalizes names to title-case across `udhaar` and `sales` tables.

---

## Supplier Balance Logic

```sql
SELECT s.id, s.name, s.phone,
  SUM(CASE WHEN p.type = 'BILL' THEN p.total_amount ELSE -p.total_amount END) as balance,
  MAX(p.timestamp) as last_time
FROM suppliers s
LEFT JOIN purchases p ON s.id = p.supplier_id AND p.is_deleted = 0
WHERE s.is_deleted = 0
GROUP BY s.id, s.name, s.phone
```
- **Positive balance** = store owes supplier.
- **Negative balance** = supplier has overpayment / advance.

---

## Input Sanitization

### Android (`StringUtils.kt`)
```kotlin
fun sanitize(input: String?): String {
    if (input == null) return ""
    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
        .replace("/", "&#x2F;")
}
```

### Web (`sanitize.ts`)
```typescript
export function sanitizeInput(input: string | null | undefined): string {
  if (!input) return '';
  return input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;');
}
```

**Parity**: ✅ Identical character replacements on both platforms.

**Usage**:
- Android: Applied selectively (not consistently applied to all input fields — UNCONFIRMED if used before every DB write).
- Web: Applied to all `onChange` handlers in login page inputs. Applied to `createStaffAccount()` username.

---

## Security

### EncryptedSharedPreferences (`SecurityUtils.kt`)
- Uses `MasterKey.KeyScheme.AES256_GCM` with `AES256_SIV` key encryption and `AES256_GCM` value encryption.
- Auto-migrates data from plaintext `storebook_prefs` to encrypted `storebook_secure_prefs`.
- Fallback: On OEM-specific failure, falls back to standard SharedPreferences (documented as not recommended for strict security).

### Session Security (`session.ts`)
- Session cookies are `httpOnly` and `secure` (in production).
- **IDOR mitigation**: `getSession()` validates `activeStoreId` cookie against user's `stores[]` array or `storeId` from database.
  - Staff: locked to their assigned `storeId`, cookie ignored.
  - Owner: validated against owned stores. Falls back to first owned store if cookie is forged.
  - Admin: can access any store.

---

## Currency Formatting

**Android** (`CurrencyUtils.kt`): Uses `NumberFormat.getCurrencyInstance(Locale("en", "IN"))` for ₹ formatting.

**Web** (`FormattedAmount.tsx`): Component rendering with `₹` prefix.

---

## Export Functions

### Android
- **Excel**: `ExcelExporter.kt` generates `.xlsx` files for items, sales, udhaar, expenses.
- **PDF (Invoice)**: `InvoicePdfGenerator.kt` generates GST-compliant tax invoices.
- **PDF (Ledger)**: `UdhaarPdfGenerator.kt` generates customer statement PDFs.

### Web
- **CSV/Excel**: `ExportButtons.tsx` — client-side CSV generation from Redux state.
