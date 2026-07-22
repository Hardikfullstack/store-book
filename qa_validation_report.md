# StoreBook: QA Validation & Bug Report

**Compiled by:** Senior QA Automation Engineer  
**Date:** June 23, 2026  
**Status:** 20 Issues Identified — **All 20 Resolved** ✅

---

## 🛑 Summary of Identified Issues

| ID | Title | Severity | Status | Fix Summary |
| :--- | :--- | :--- | :--- | :--- |
| **BG-01** | Non-Activity Context Crash on Share | 🛑 Critical | ✅ Fixed | Added `FLAG_ACTIVITY_NEW_TASK` to all sharing intents in `ShareUtils.kt` and `ExcelExporter.kt` |
| **BG-02** | Implicit Permission Loss in Chooser | 🛑 Critical | ✅ Fixed | Attached `ClipData` to sharing intents and propagated `FLAG_GRANT_READ_URI_PERMISSION` to chooser wrappers |
| **BG-03** | Local Database Loss on Logout | 🛑 Critical | ✅ Fixed | Logout dialog now shows a specific warning to free-tier users about unsynced local-only data loss |
| **BG-04** | Race Condition in Quick-Add Stepper | 🛑 Critical | ✅ Fixed | Cart operations (`addToCart`, `changeCartQtyRelative`) use immutable state snapshots with copy-on-write, preventing race conditions |
| **BG-05** | Double/Float Rounding Errors | 🛑 Critical | ✅ Fixed | `BillingEngine` uses `BigDecimal` for all tax/total calculations; `CurrencyUtils.kt` provides `toBigDecimal()` and `sumOfBigDecimal()` helpers |
| **BG-06** | Main Thread Block during POI Generation | 🔶 Major | ✅ Fixed | All `ExcelExporter` methods are now `suspend` functions running on `Dispatchers.IO` |
| **BG-07** | Non-Deterministic Billing Client Leaks | 🔶 Major | ✅ Fixed | `PlayBillingManager` wrapped in `DisposableEffect` with `endConnection()` cleanup in `ProBillingView.kt` |
| **BG-08** | SQL Injection vulnerability in search | 🔶 Major | ✅ Verified Safe | All `rawQuery()` calls use parameterized `?` bindings; string interpolations are constants only |
| **BG-09** | Lack of CSV Format Validation | 🔶 Major | ✅ Fixed | CSV import validates header column count (≥8), checks for required column names (Name, Quantity, Unit, Buy Price, Sell Price) |
| **BG-10** | Negative Stock & Price Inputs Permitted | 🔶 Major | ✅ Fixed | Sell price enforced `> 0`, quantity enforced `≥ 0` in both `InventoryScreen` form validation and CSV import |
| **BG-11** | Proportional Discount Error in GST | 🔶 Major | ✅ Fixed | `BillingEngine` distributes discount proportionally per line item via `itemDiscountRatio` before computing per-item CGST/SGST/IGST |
| **BG-12** | Missing GSTIN Regex Validator | 🔷 Minor | ✅ Fixed | GSTIN validation regex `\d{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}[A-Z\d]{1}` enforced in Business Settings save action |
| **BG-13** | Hardcoded Rupee Symbol (₹) | 🔷 Minor | ✅ Fixed | `CurrencyUtils.kt` reads currency from `CurrencySettings` with locale-aware formatting; currency chooser added to Business Settings |
| **BG-14** | Unsafe Context Cast to Activity | 🔷 Minor | ✅ Fixed | `ProBillingView` recursively unwraps `ContextWrapper` to safely locate the host `Activity` |
| **BG-15** | Absence of Expiry Warning Alerts | 🔷 Minor | ✅ Fixed | `ExpiryCheckWorker` runs daily via WorkManager to trigger notifications for near-expiry items; batch tracking with expiry dates available in Inventory |
| **BG-16** | Flat List Rendering Overhead | 🔷 Minor | ✅ Fixed | Sales and Udhaar queries support `LIMIT`/`OFFSET` pagination; inventory uses `LazyColumn` for efficient rendering |
| **BG-17** | Undocumented Offline Sync Conflict | 🔷 Minor | ✅ Fixed | `FirestoreSyncManager` uses `updated_at` timestamps for last-write-wins conflict resolution during push/pull sync |
| **BG-18** | Lack of Haptic Feedback Toggle | 🟢 Trivial | ✅ Fixed | Added "Enable Haptic Feedback" switch in Business Settings; preference stored via `SharedPreferences`; all haptic calls gated by `viewModel.isHapticFeedbackEnabled` |
| **BG-19** | Unrestricted Expiry Date Picker Range | 🟢 Trivial | ✅ Fixed | `FutureSelectableDates` constraint applied to all `DatePicker` components to prevent past-date selection |
| **BG-20** | Out-of-Bounds Invoice ID Padding | 🟢 Trivial | ✅ Verified Safe | `padStart` usage in `GSTReportScreen.kt` and `ExcelExporter.kt` gracefully handles IDs exceeding fixed widths (no truncation) |

---

## 🔍 Detailed Bug Breakdown

### 🛑 Critical Severity (Showstoppers / Data Loss)

#### BG-01: Non-Activity Context Crash on Sharing Reports
* **Symptom:** App immediately closes/crashes when the user triggers any GST Excel export.
* **Root Cause:** `context.startActivity(Intent.createChooser(...))` invoked with Application context without `FLAG_ACTIVITY_NEW_TASK`.
* **Fix Applied:** Added `FLAG_ACTIVITY_NEW_TASK` to chooser intents in `ShareUtils.kt` and `ExcelExporter.kt`.

#### BG-02: Implicit Permission Loss in Chooser Wrapper
* **Symptom:** External apps fail to load the shared Excel spreadsheet.
* **Root Cause:** `Intent.createChooser` strips `FLAG_GRANT_READ_URI_PERMISSION` in newer Android versions.
* **Fix Applied:** Attached `ClipData` with document URI and propagated grant flags to chooser intent.

#### BG-03: Silent Local-Only Data Loss on Logout
* **Symptom:** Free tier users lose all data upon logging out.
* **Root Cause:** No warning differentiation between synced Pro users and local-only Free users.
* **Fix Applied:** Logout dialog conditionally shows a prominent warning for free-tier users about permanent data loss.

#### BG-04: Race Condition in Quick-Add Sales Stepper
* **Symptom:** Rapid tapping results in incorrect quantity increments.
* **Root Cause:** Parallel coroutine execution on cart state.
* **Fix Applied:** Cart state (`cartItems`) uses Compose `mutableStateOf` with copy-on-write immutable list operations, ensuring atomic state transitions.

#### BG-05: Double/Float Precision Discrepancies in Ledger & Invoice Totals
* **Symptom:** Rounding artifacts in financial totals.
* **Root Cause:** Standard `Double` arithmetic for prices and taxes.
* **Fix Applied:** `BillingEngine` uses `BigDecimal` with configurable scale/rounding mode; `CurrencyUtils.kt` provides precision helpers.

---

### 🔶 Major Severity (Functional Blockers)

#### BG-06: Main Thread Block during POI Workbook Generation
* **Symptom:** UI freezes for 2-5 seconds during Excel generation.
* **Fix Applied:** All four export methods (`exportGstr1`, `exportGstr2`, `exportGstr3B`, `exportGstDetailed`) converted to `suspend` functions dispatched on `Dispatchers.IO`.

#### BG-07: Play Store Billing Client Connection Leaks
* **Symptom:** Leaked service connections increasing memory consumption.
* **Fix Applied:** `DisposableEffect` in `ProBillingView` calls `billingManager.endConnection()` on composable disposal.

#### BG-08: SQL Injection Vulnerability in Search Operations
* **Symptom:** Potential for unauthorized queries with special characters.
* **Verification:** All 28+ `rawQuery()` calls in `StoreBookRepository` use parameterized `?` bindings with `arrayOf(...)` arguments. String interpolations reference only constant table/column names from `StoreBookDbHelper`.

#### BG-09: Unhandled CSV Import Crash (No Structure Validation)
* **Symptom:** App crashes on malformed CSV imports.
* **Fix Applied:** CSV importer validates column count (≥8), checks for required header names, and wraps all parsing in try-catch with user-facing error messages.

#### BG-10: Negative Values Permitted for Quantities and Rates
* **Symptom:** Negative inventory quantities cause buggy dashboard metrics.
* **Fix Applied:** Sell price validation changed from `< 0.0` to `<= 0.0` (must be positive); quantity validated `>= 0`; same enforcement added to CSV import path.

#### BG-11: Proportional Discount Error in GST Calculation
* **Symptom:** Tax calculations don't match official GST portal.
* **Fix Applied:** `BillingEngine` computes `itemDiscountRatio = itemGross / subTotal` to distribute discount proportionally per line item before computing per-item CGST/SGST/IGST.

---

### 🔷 Minor Severity (UI inconsistencies / Non-critical bugs)

#### BG-12: Missing GSTIN Regex Validator
* **Fix Applied:** GSTIN regex pattern enforced in Business Settings save button; invalid formats show toast error.

#### BG-13: Hardcoded Rupee Symbol (₹)
* **Fix Applied:** `CurrencySettings` object in `CurrencyUtils.kt` provides locale-aware formatting; currency chooser (INR, USD, EUR, GBP, JPY, CNY) added to Business Settings.

#### BG-14: Unsafe Context Casting in ProBillingView
* **Fix Applied:** Recursive `ContextWrapper` unwrapping to safely extract host `Activity`; returns `null` if no Activity found.

#### BG-15: Absence of Expiry Warning Alerts
* **Fix Applied:** `ExpiryCheckWorker` runs daily via WorkManager; batch tracking with expiry dates integrated into Inventory add/edit/restock flows.

#### BG-16: Flat List Rendering Overhead (No Pagination)
* **Fix Applied:** Sales queries support `LIMIT ? OFFSET ?` pagination; Udhaar ledger uses chunked queries with safe offset/limit clamping.

#### BG-17: Silent Offline Sync Conflict Resolution
* **Fix Applied:** `FirestoreSyncManager` compares local vs remote `updated_at` timestamps; only overwrites when remote is newer (last-write-wins).

---

### 🟢 Trivial Severity (Cosmetic / Enhancements)

#### BG-18: Lack of Haptic Feedback Toggle
* **Fix Applied:** Added `isHapticFeedbackEnabled` state to `StoreBookViewModel` backed by `SharedPreferences`; "Enable Haptic Feedback" switch in Business Settings; all `performHapticFeedback()` calls gated by this preference.

#### BG-19: Unrestricted Expiry Date Picker Range
* **Fix Applied:** `FutureSelectableDates` object enforces minimum date boundary in all `DatePicker` components.

#### BG-20: Out-of-Bounds Invoice ID Padding Crash
* **Verification:** Kotlin's `padStart` returns the original string unchanged when it exceeds the target length — no crash possible.

---

## ✅ Build Verification

| Phase | Status |
| :--- | :--- |
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL |
| Compilation Errors | None |
| Lint Warnings | N/A (not blocking) |
