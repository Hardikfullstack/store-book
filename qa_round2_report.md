# StoreBook — QA Round 2 Validation Report

**Compiled by:** Senior QA Automation Engineer  
**Date:** June 23, 2026  
**Scope:** Full codebase re-audit post Round-1 fixes  
**Status:** 20 New Issues Identified

---

## 🔴 Summary Table

| ID | Title | Screen/File | Severity |
|:---|:---|:---|:---|
| **BG2-01** | `nearExpiryItems` populated with duplicates per item | ViewModel | 🛑 Critical |
| **BG2-02** | `getActiveItemsFiltered()` loads ALL items into memory regardless of offset | Repository | 🛑 Critical |
| **BG2-03** | Undo-last-sale countdown not cancelled on screen leave / new sale | DashboardScreen | 🛑 Critical |
| **BG2-04** | Stock oversell — no real-time stock guard at checkout | SalesScreen | 🛑 Critical |
| **BG2-05** | Udhaar "CREDIT" vs "PAYMENT" sign logic inverted in ledger | UdhaarScreen | 🛑 Critical |
| **BG2-06** | `ExpiryCheckWorker` sends identical notification ID (1001) always | ExpiryCheckWorker | 🔶 Major |
| **BG2-07** | `filterMode` ("NearExpiry") not reset when category chip is selected | InventoryScreen | 🔶 Major |
| **BG2-08** | Empty-state message ignores `filterMode` — misleads user in expiry view | InventoryScreen | 🔶 Major |
| **BG2-09** | Discount field accepts values greater than cart total — negative grand total possible | SalesScreen | 🔶 Major |
| **BG2-10** | Dashboard greeting `hourOfDay` captured once with `remember {}` — never refreshes | DashboardScreen | 🔶 Major |
| **BG2-11** | `fetchSalesFromCursor` performs a second unbounded IN-query with no LIMIT | Repository | 🔶 Major |
| **BG2-12** | UdhaarScreen creates a secondary `StoreBookRepository` instance — bypasses ViewModel cache | UdhaarScreen | 🔶 Major |
| **BG2-13** | GST Report loads data on Main thread inside `LaunchedEffect` without `Dispatchers.IO` | GSTReportScreen | 🔶 Major |
| **BG2-14** | `ExpiryCheckWorker` — no `POST_NOTIFICATIONS` permission check before showing notification | ExpiryCheckWorker | 🔷 Minor |
| **BG2-15** | Inventory "Near Expiry" filter shows item-level list but batches may belong to deleted items | InventoryScreen/ViewModel | 🔷 Minor |
| **BG2-16** | `cartItems` state not cleared after estimate save — stale cart persists when returning | SalesScreen/ViewModel | 🔷 Minor |
| **BG2-17** | `UdhaarEntry` amount stored without sign convention — negative payment entries possible | Repository | 🔷 Minor |
| **BG2-18** | `SalesScreen` search filters only loaded `allItems` — new items added mid-session not reflected | SalesScreen | 🔷 Minor |
| **BG2-19** | Avatar letter extraction hard-codes `+91` — breaks for international numbers | DashboardScreen | 🟢 Trivial |
| **BG2-20** | "Expiring ≤30d" chip in inventory has no badge/count indicator | InventoryScreen | 🟢 Trivial |

---

## 🔴 Critical Severity (Data Integrity / Crashes)

---

### BG2-01 — `nearExpiryItems` populated with duplicates per item
**File:** `StoreBookViewModel.kt` (line ~370)  
**Description:**  
`getNearExpiryBatches(30)` returns one row **per batch**. Multiple batches can exist for the same `itemId`. The code calls `getItemById(batch.itemId)` for each batch, inserting the same `Item` multiple times into `_nearExpiryItems`.

**Reproduction:**
1. Add item "Milk" with 2 batches, both expiring within 30 days.
2. Open Inventory → select "Expiring ≤30d".
3. "Milk" appears **twice** in the list.

**Impact:** Incorrect item count display, confusing UX, inflated "Expiring ≤30d" badge count.

**Fix:** Deduplicate by `itemId` before assigning to `_nearExpiryItems`:
```kotlin
_nearExpiryItems.value = nearBatches
    .mapNotNull { batch -> repository.getItemById(batch.itemId) }
    .distinctBy { it.id }
```

---

### BG2-02 — `getActiveItemsFiltered()` loads ALL items into memory before pagination
**File:** `StoreBookRepository.kt` (line 661)  
**Description:**  
`getActiveItemsFiltered()` calls `getActiveItems()` (which fetches all rows from SQLite), applies in-memory filtering and sorting, then slices with `subList`. For a store with 10,000 items, this allocates a massive list every time the search debounce fires (every 300ms).

**Impact:** OOM risk, UI jank (even on `Dispatchers.IO`), battery drain from repeated full-table scans.

**Fix:** Push `WHERE`, `ORDER BY`, `LIMIT`, and `OFFSET` clauses directly into the SQL `rawQuery`.

---

### BG2-03 — Undo-last-sale countdown not cancelled when screen leaves or new sale completes
**File:** `DashboardScreen.kt` (line 230–238)  
**Description:**  
A `while (undoSecondsLeft > 0) { delay(1000); undoSecondsLeft-- }` loop runs inside `LaunchedEffect`. When the user navigates away and returns, a **second** `LaunchedEffect` restarts the loop while the first is still running (the coroutine is not automatically cancelled in all lifecycle transitions under `LazyColumn` recomposition). This can result in two parallel countdown loops fighting each other.

**Impact:** Counter may flicker between two values, or "Undo" card remains visible after it should have expired.

**Fix:** Use a single controlled coroutine via `rememberCoroutineScope` and cancel the previous job explicitly on each new `lastSaleId`.

---

### BG2-04 — No real-time stock guard at checkout — oversell possible
**File:** `SalesScreen.kt` / `StoreBookRepository.kt`  
**Description:**  
`checkout()` in the ViewModel deducts stock based on the cart quantities formed when the item was added. If another device (or another tab) sells the same item concurrently and syncs, the stock value in the local DB may already be lower than the cart quantity. There is no `WHERE quantity >= ?` guard in the `UPDATE` SQL for stock deduction.

**Impact:** Stock quantity can go negative, corrupting ledger and dashboard stats.

**Fix:** Add an optimistic lock in the stock UPDATE:
```sql
UPDATE items SET quantity = quantity - ? WHERE id = ? AND quantity >= ?
```
If rows affected = 0, abort the transaction and surface a conflict error to the user.

---

### BG2-05 — Udhaar "CREDIT" / "PAYMENT" sign logic inverted in net balance display
**File:** `UdhaarScreen.kt` (line 126–128)  
**Description:**  
`totalOutstanding` is calculated as `balances.filter { it.netBalance > 0 }.sumOf { it.netBalance }`. A positive `netBalance` means the customer owes the shop — which is correct. However, in the customer ledger detail view, entries of type "PAYMENT" from the customer are stored with a **positive** amount, making `netBalance` go up instead of down.

**Impact:** Paying a customer's debt increases their displayed outstanding balance instead of decreasing it.

**Fix:** Enforce sign convention: `CREDIT` stored as positive, `PAYMENT` stored as negative. Apply this consistently in `insertUdhaarEntry` and `getUdhaarBalances`.

---

## 🔶 Major Severity

---

### BG2-06 — `ExpiryCheckWorker` always notifies with same ID (1001)
**File:** `ExpiryCheckWorker.kt` (line 52)  
**Description:**  
`manager.notify(1001, notification)` always uses the same notification ID. If the worker fires multiple times in a day (e.g., user manually enqueues it), the new notification silently replaces the old one without alerting. More critically, if the app sends a low-stock notification with the same ID elsewhere, the expiry alert is overwritten.

**Fix:** Use a stable but unique ID (e.g., `EXPIRY_NOTIFICATION_ID = 2001`) distinct from other channels.

---

### BG2-07 — Selecting a category chip does NOT reset `filterMode` to "All"
**File:** `InventoryScreen.kt` (line 700–719)  
**Description:**  
When the user is in "Expiring ≤30d" `filterMode` and taps a category chip (e.g., "Groceries"), `selectedCategory` changes but `filterMode` remains "NearExpiry". The `LaunchedEffect(filterMode, filteredItems, nearExpiryItems)` returns `nearExpiryItems` regardless, so the category selection has **no effect**.

**Reproduction:**
1. Tap "Expiring ≤30d" → list shows expiry items.
2. Tap "Groceries" category chip.
3. List still shows all near-expiry items (ignores Groceries filter).

**Fix:** Reset `filterMode = "All"` inside each category chip's `onClick`.

---

### BG2-08 — Empty-state message doesn't account for `filterMode == "NearExpiry"`
**File:** `InventoryScreen.kt` (line 750–753)  
**Description:**  
When no items are expiring within 30 days, `displayedItems` is empty and the empty-state shows: *"No stock yet? Your first item is just a tap away!"* — which is completely wrong context.

**Fix:**
```kotlin
text = when {
    filterMode == "NearExpiry" -> "🎉 No items expiring in the next 30 days!"
    searchQ.isBlank() && selectedCategory == "All" -> "No stock yet!..."
    else -> stringResource(R.string.search_no_results)
}
```

---

### BG2-09 — Discount field has no upper-bound validation — negative grand total possible
**File:** `SalesScreen.kt` (line 516–531)  
**Description:**  
`cartDiscount` is set directly from user input with no cap. A user can type a discount larger than the cart subtotal, causing `grandTotal` to go negative. The checkout proceeds without error, recording a negative total sale in the DB.

**Fix:** Clamp discount on input: `viewModel.cartDiscount = (it.toDoubleOrNull() ?: 0.0).coerceAtMost(subtotal)`

---

### BG2-10 — Dashboard greeting `hourOfDay` captured once and never refreshes
**File:** `DashboardScreen.kt` (line 116)  
**Description:**  
`val hourOfDay = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }` — `remember {}` with no keys caches the value for the lifetime of the composable. If the app stays open past midnight, the greeting stays "🌙 Good Evening" all morning.

**Fix:** Use `remember(System.currentTimeMillis() / (3600 * 1000))` — key on the current hour so it recomputes when the hour changes.

---

### BG2-11 — `fetchSalesFromCursor` uses unbounded `IN (...)` query for sale items
**File:** `StoreBookRepository.kt` (line 613–617)  
**Description:**  
```kotlin
val placeholders = saleIds.joinToString(",") { "?" }
db.rawQuery("SELECT * FROM sale_items WHERE sale_id IN ($placeholders)", ...)
```
SQLite has a limit of **999 bound parameters**. If `getSalesPage` is called with 1000+ sales (e.g., during a full data export), this query will throw `SQLiteException: too many SQL variables`.

**Fix:** Batch the IN-query in chunks of 500.

---

### BG2-12 — `UdhaarScreen` instantiates its own `StoreBookRepository` — bypasses ViewModel
**File:** `UdhaarScreen.kt` (lines 135–143)  
**Description:**  
```kotlin
val repository = remember(viewModel.activeStoreId) {
    StoreBookRepository(context.applicationContext, viewModel.activeStoreId)
}
```
This creates a **second** database helper instance, opening a second connection to the same SQLite file. Under concurrent writes this can cause `SQLiteDatabaseLockedException`.

**Fix:** Expose `getCustomerLedger(name)` through the ViewModel as a suspend function and call it from there.

---

### BG2-13 — `GSTReportScreen` performs DB queries on Main thread via `LaunchedEffect`
**File:** `GSTReportScreen.kt` (lines 87–94)  
**Description:**  
```kotlin
LaunchedEffect(startTs, endTs) {
    sales = viewModel.repository.getSalesByDateRange(startTs, endTs)
    purchases = viewModel.repository.getPurchasesByDateRange(startTs, endTs)
    ...
}
```
`LaunchedEffect` runs in the **main coroutine dispatcher** unless the called functions switch context internally. `getSalesByDateRange` does use `withContext(Dispatchers.IO)`, but `getAllSuppliersMap()` and `getAllItemsMap()` should be verified — if they don't switch context, they block the main thread.

**Fix:** Wrap the entire `LaunchedEffect` block with `withContext(Dispatchers.IO) { ... }` as a safety net.

---

## 🔷 Minor Severity

---

### BG2-14 — `ExpiryCheckWorker` posts notification without checking `POST_NOTIFICATIONS` permission (Android 13+)
**File:** `ExpiryCheckWorker.kt`  
**Description:**  
On Android 13+ (API 33), posting notifications without the `POST_NOTIFICATIONS` runtime permission throws a `SecurityException`. The worker calls `manager.notify(...)` without checking `ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS)`.

**Fix:** Guard with permission check or use `try/catch` around the notify call.

---

### BG2-15 — "Expiring ≤30d" filter may show items that are soft-deleted
**File:** `StoreBookViewModel.kt` + `StoreBookRepository.kt`  
**Description:**  
`getNearExpiryBatches` filters `WHERE is_deleted = 0` on batches, but the parent `Item` may have been soft-deleted (`item_is_deleted = 1`). `getItemById` returns the item regardless of its deletion status because the query does not include `WHERE item_is_deleted = 0`.

**Fix:** Add `AND is_deleted = 0` to the `getItemById` query, or filter in the ViewModel after fetch.

---

### BG2-16 — Cart not cleared after saving an Estimate — stale cart persists
**File:** `SalesScreen.kt` (line 866–872)  
**Description:**  
When the user saves as an Estimate (`viewModel.checkout(..., type = "ESTIMATE")`), the cart is cleared in the ViewModel. However if the user then presses back from `SalesSuccessScreen` (which calls `showSuccessScreen = false`), the previous cart state is already gone but `viewModel.cartCustomerName`, `viewModel.cartDiscount`, and `viewModel.cartCustomerGstin` are NOT reset.

**Impact:** Next invoice pre-fills stale customer/discount from the previous estimate.

**Fix:** Call `viewModel.clearCart()` (or equivalent reset) on success screen dismiss.

---

### BG2-17 — `UdhaarEntry.amount` has no enforced sign convention — negative values storable
**File:** `StoreBookRepository.kt` insertUdhaarEntry  
**Description:**  
The `amount` field in `UdhaarEntry` is stored as-is. If the UI passes a negative value (e.g., user enters "-500"), it is stored without validation. This breaks the `netBalance` calculation which assumes positive amounts for both CREDIT and PAYMENT types.

**Fix:** `coerceAtLeast(0.0)` before inserting, and assert amount > 0 in the UI.

---

### BG2-18 — SalesScreen search only filters `allItems` at composition time — misses new items
**File:** `SalesScreen.kt` (lines 164–172)  
**Description:**  
```kotlin
val filteredItems by remember {
    derivedStateOf {
        if (searchQ.isBlank()) allItems
        else allItems.filter { it.name.contains(searchQ, ignoreCase = true) }
    }
}
```
`allItems` is the ViewModel's cached list loaded at screen open. If a new item is added from another screen (e.g., Inventory) while the Sales screen is in the back stack, the search does not reflect the new item until a full `loadAllData()` is triggered.

**Fix:** Call `viewModel.loadAllData()` (or at minimum `loadFilteredItems`) in `LaunchedEffect(Unit)` in `SalesScreen`.

---

### BG2-19 — Avatar letter hard-codes `+91` country prefix — breaks for international users
**File:** `DashboardScreen.kt` (line 410)  
**Description:**  
```kotlin
phone.replace("+91", "").trim().take(1).uppercase()
```
For a UK number `+447911123456`, replacing `+91` yields `47911123456` and shows `4` instead of the first meaningful digit. For a US number `+12025551234`, replacing `+91` does nothing, showing `+`.

**Fix:** Use `phone.filter { it.isDigit() }.take(1)` or parse with `PhoneNumberUtils`.

---

### BG2-20 — "Expiring ≤30d" chip has no count badge — user can't see urgency at a glance
**File:** `InventoryScreen.kt`  
**Description:**  
The filter chip label is static `"🕒 Expiring ≤30d"`. Other inventory filters (Low Stock) show the count inline. Without a count badge, the user cannot tell if they have 0 or 50 near-expiry items before tapping.

**Fix:** Show count from `nearExpiryItems.size`:
```kotlin
label = "🕒 Expiring ≤30d${if (nearExpiryItems.isNotEmpty()) " (${nearExpiryItems.size})" else ""}"
```

---

## ✅ Build Reference

| Phase | Status |
|:---|:---|
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL |
| Compilation Errors | None |
