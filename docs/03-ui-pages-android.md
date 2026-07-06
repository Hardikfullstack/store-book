# 03 — UI Pages (Android)

All screens use Jetpack Compose and are hosted within a single `MainActivity`. Navigation is managed via `AppNavigation.kt` using the Compose Navigation library with typed `Routes` sealed class.

---

## Splash Screen
- **Route**: `Routes.Splash` (`splash_screen`)
- **File**: `SplashScreen.kt`
- **Purpose**: Initial loading screen shown on app launch. Checks Firebase Auth state and redirects to Auth or Dashboard.
- **Data fetched**: Firebase Auth `currentUser` check.
- **Next screen**: If logged in → Dashboard. If not → Auth.

---

## Auth Screen
- **Route**: `Routes.Auth` (`auth_screen`)
- **File**: `AuthScreen.kt` (48KB)
- **Purpose**: Phone OTP login (via Firebase Auth) and Google Sign-In.
- **Layout**: Country code selector, phone number input, OTP input, Google Sign-In button, language selector.
- **Auth Flow**:
  1. User enters phone → `signInWithPhoneNumber()` via Firebase.
  2. OTP sent → user enters 6-digit code → `signInWithCredential(PhoneAuthProvider.getCredential())`.
  3. On success → checks if user exists in Data Connect via `GetUser` query.
  4. If new user → calls `SyncUser` mutation (PUBLIC auth) to create user record.
  5. If user has no store → creates store via `SyncStore` mutation, updates user's `stores` array.
  6. Triggers initial `SyncWorker` to pull cloud data into local SQLite.
  7. Navigates to Dashboard.
- **State**: `StoreBookViewModel` manages auth state (`isLoggedIn`, `currentUser`, `storeId`).
- **Permissions**: None required; this is the entry point.

---

## Onboarding Screen
- **Route**: `Routes.Language` (`language_screen`)
- **File**: `OnboardingScreen.kt`
- **Purpose**: First-time language selection and store naming.
- **Fields**: Language picker (Hindi, English, etc.), store name input.
- **Data submitted**: Language preference saved to SharedPreferences. Store name used during store creation.

---

## Dashboard Screen
- **Route**: `Routes.Dashboard` (`dashboard_screen`)
- **File**: `DashboardScreen.kt` (61KB)
- **Purpose**: Main home screen showing business summary.
- **Data fetched on load**: `getSales()`, `getExpenses()`, `getUdhaarBalances()`, `getActiveItems()`, `getNearExpiryBatches()` from local SQLite.
- **Layout**:
  - Top bar with store name, sync status, premium badge
  - Summary cards: Today's Sales, Expenses, Udhaar balance, Inventory count
  - Low-stock alerts
  - Near-expiry batch warnings
  - Revenue chart (daily/weekly/monthly)
  - Quick action buttons (New Sale, Add Item, etc.)
- **State**: `StoreBookViewModel` — `salesList`, `expensesList`, `udhaarBalances`, `items`
- **Sync trigger**: Pull-to-refresh triggers `SyncWorker.performSync()`.
- **Bottom Navigation**: Dashboard / Inventory / Sales / Udhaar / More

---

## Inventory Screen
- **Route**: `Routes.Inventory` (`inventory_screen`)
- **File**: `InventoryScreen.kt` (100KB)
- **Purpose**: Full inventory management — list, add, edit, delete, filter, sort, restock items.
- **Data fetched**: `getActiveItemsFiltered()` with search, category, sort params.
- **Fields (Add/Edit Item)**:

| Field | Label | Type | Required | Validation |
|---|---|---|---|---|
| name | Item Name | String | Yes | Non-empty, unique (SQLite UNIQUE constraint) |
| quantity | Quantity | Double | Yes | ≥ 0 |
| unit | Unit | String | Yes | Non-empty (e.g., kg, pcs, litre) |
| buyPrice | Buy Price | Double | Yes | ≥ 0 |
| sellPrice | Sell Price | Double | Yes | ≥ 0 |
| category | Category | String | Yes | Non-empty |
| lowStockThreshold | Low Stock Alert | Double | No | ≥ 0 |
| hsnCode | HSN Code | String | No | — |
| taxRate | GST Rate (%) | Double | No | 0–100 |
| photoPath | Photo | File URI | No | Camera/gallery picker |

- **DB write path**: Field → ViewModel state → `repository.insertItem(item)` / `updateItem(item)` → SQLite `items` table with `is_synced=0`.
- **Delete**: Soft-delete via `repository.softDeleteItem(id)`. Deleted items recoverable via "Recycle Bin" in MoreScreen.
- **Restock**: Bottom sheet with quantity, cost price, supplier fields → `repository.restockItem()` → updates item qty + creates expense entry in single transaction.
- **Sort options**: Name (A-Z/Z-A), Price (High/Low), Quantity (High/Low), Recent.
- **Category filter**: "All", user-defined categories, "Low Stock" (virtual filter).
- **Alphabet scrubber**: `AlphabetScrubber.kt` component for fast scrolling.

---

## Sales Screen (POS)
- **Route**: `Routes.Sales` (`sales_screen`)
- **File**: `SalesScreen.kt` (93KB)
- **Purpose**: Point-of-sale cart, checkout flow, invoice generation.
- **Layout**: Item search/add, cart list, discount input, customer details, payment mode selector, checkout button.
- **Cart management**: `CartItem(item, quantity)` objects held in ViewModel state.
- **Checkout fields**:

| Field | Label | Required | Validation |
|---|---|---|---|
| discount | Discount (₹) | No | 0 ≤ discount ≤ subtotal (clamped by BillingEngine) |
| customerName | Customer Name | No (required for Udhaar) | — |
| customerGstin | Customer GSTIN | No | — |
| businessGstin | Business GSTIN | No | — |
| paymentMode | Payment Mode | Yes | Cash / UPI / Udhaar |
| notes | Notes | No | — |

- **DB write path**: Checkout → `repository.recordSale()` → SQLite transaction:
  1. Calculate tax via `BillingEngine.calculateInvoiceTaxes()`
  2. Insert `sales` row
  3. Insert `sale_items` rows for each cart item
  4. Deduct item quantities (skip for `ESTIMATE` type and dummy items with `id=0`)
  5. If payment mode = "Udhaar" and customerName provided → auto-insert `udhaar` CREDIT entry
- **Stock oversell guard**: Checks `currentQty >= cartItem.quantity` before deducting. Throws exception if insufficient.
- **Quick Cash Sale**: Quick-entry mode with item name + price (creates dummy item with `id=0`).
- **Invoice PDF**: Generated via `InvoicePdfGenerator.kt` using sale data + BillingEngine tax breakdown.

---

## Sales History Screen
- **Route**: `Routes.SalesHistory` (`sales_history_screen`)
- **File**: `SalesHistoryScreen.kt`
- **Purpose**: Paginated list of past sales with undo capability.
- **Data fetched**: `repository.getSalesPage(limit, offset)`.
- **Actions**: View sale details, Undo sale (`repository.undoSale()` — restores stock, soft-deletes sale + items + linked udhaar).

---

## Sales Analytics Screen
- **Route**: `Routes.SalesAnalytics` (`sales_analytics_screen`)
- **File**: `SalesAnalyticsScreen.kt` (42KB)
- **Purpose**: Date-range profit/loss reports, revenue graphs.
- **Data fetched**: `getSalesByDateRange()`, `getExpenses()`, `getPurchasesByDateRange()`.
- **Computed fields**: Revenue = ΣsaleTotal, Cost = Σ(buyPrice × qty), Gross Profit = Revenue - Cost, Net Profit = Gross Profit - Expenses.

---

## Udhaar Screen
- **Route**: `Routes.Udhaar` (`udhaar_screen`)
- **File**: `UdhaarScreen.kt` (68KB)
- **Purpose**: Customer credit/debit ledger management.
- **Layout**: Customer balance list (alphabet scrubber), customer detail view with transaction history, add credit/payment form.
- **Balance calculation**: `repository.getUdhaarBalances()` → SQL `SUM(CASE WHEN type='CREDIT' THEN amount ELSE -amount END)`.
- **Fields (Add entry)**:

| Field | Required | Validation |
|---|---|---|
| customerName | Yes | Non-empty |
| amount | Yes | > 0, `coerceAtLeast(0.0)` enforced in repo |
| type | Yes | `CREDIT` or `PAYMENT` |
| notes | No | — |

- **PDF export**: `UdhaarPdfGenerator.kt` generates customer ledger PDF.

---

## More Screen
- **Route**: `Routes.More` (`more_screen`)
- **File**: `MoreScreen.kt` (143KB — largest screen)
- **Purpose**: Hub for settings, expenses, tools, export, and secondary features.
- **Sub-sections**:
  - **Expenses**: Add/view expense entries (type: RESTOCK/OVERHEAD).
  - **Business Profile**: Store name, business GSTIN, business address (saved to SharedPreferences).
  - **Export**: Excel export via `ExcelExporter.kt` (items, sales, udhaar, expenses).
  - **Theme**: Light/Dark/Auto mode toggle.
  - **Language**: Language change.
  - **Recycle Bin**: View and restore soft-deleted items.
  - **Clear Data**: `repository.clearLocalDatabase()` — wipes all local tables.
  - **Sync**: Manual sync trigger.
  - **Premium**: Navigate to ProBillingView.
  - **Share App**: Via `ShareUtils.kt`.
  - **Rate & Review**: Via `ReviewUtils.kt`.

---

## Quotation Screen
- **Route**: `Routes.Quotations` (`quotations_screen`)
- **File**: `QuotationScreen.kt`
- **Purpose**: View estimates/quotations, convert to actual sale.
- **Data fetched**: `repository.getQuotations()` (type = `ESTIMATE` or `CONVERTED`).
- **Actions**: View, convert to sale (`markQuotationAsConverted()` + `recordSale()` with type `SALE`), delete.

---

## Supplier Ledger Screen
- **Route**: `Routes.SupplierLedger` (`supplier_ledger_screen`)
- **File**: `SupplierLedgerScreen.kt` (44KB)
- **Purpose**: Supplier management, purchase history, payment recording.
- **Data fetched**: `getSupplierBalances()`, `getPurchases()`, `getSuppliers()`.
- **Balance**: SQL JOIN between suppliers and purchases, `SUM(CASE WHEN type='BILL' THEN total ELSE -total END)`.
- **Actions**: Add supplier, record purchase, record payment, view purchase history.

---

## GST Report Screen
- **Route**: `Routes.GSTReport` (`gst_report_screen`)
- **File**: `GSTReportScreen.kt` (45KB)
- **Purpose**: GST compliance report (GSTR-1 style).
- **Data fetched**: `getSalesByDateRange()` with item details, taxes computed via `BillingEngine`.
- **Output**: HSN-wise tax summary, CGST/SGST/IGST breakdown.

---

## Invite Staff Screen
- **Route**: `Routes.InviteStaff` (`invite_staff_screen`)
- **File**: `InviteStaffScreen.kt`
- **Purpose**: Create staff accounts with restricted permissions.
- **Fields**: Username, PIN/password, canViewProfit toggle, canDelete toggle.
- **DB write**: Creates Firebase Auth user (email/password) + Data Connect `CreateUser` mutation.

---

## Pro Billing View
- **Route**: `Routes.PremiumPlans` (`premium_plans_screen`)
- **File**: `ProBillingView.kt` (22KB)
- **Purpose**: Premium subscription purchase flow.
- **Integration**: `PlayBillingManager.kt` → Google Play Billing Library v7.
- **Plans**: Monthly, Yearly (defined in Play Console).
