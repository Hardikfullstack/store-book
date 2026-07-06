# 08 — File Inventory

Every source file audited for this documentation, grouped by module.

---

## Android App (`app/`)

### Entry Points & Application
| File | Purpose |
|---|---|
| `app/src/main/java/com/storebook/inventoryapp/MainActivity.kt` | Single-Activity host, Compose navigation setup |
| `app/src/main/java/com/storebook/inventoryapp/StoreBookApplication.kt` | Application subclass, Firebase init, WorkManager |

### Data Layer
| File | Purpose |
|---|---|
| `app/src/main/java/com/storebook/inventoryapp/data/local/StoreBookDbHelper.kt` | SQLite schema definition (v1–v9), migrations |
| `app/src/main/java/com/storebook/inventoryapp/data/repository/StoreBookRepository.kt` | All CRUD operations, sync helpers (1880 lines) |
| `app/src/main/java/com/storebook/inventoryapp/data/model/AppResponse.kt` | Remote ad-config response model |
| `app/src/main/java/com/storebook/inventoryapp/data/billing/BillingEngine.kt` | GST tax calculation engine (CGST/SGST/IGST) |
| `app/src/main/java/com/storebook/inventoryapp/data/network/ApiClient.kt` | Retrofit client for ad-config API |
| `app/src/main/java/com/storebook/inventoryapp/data/network/ApiService.kt` | Retrofit interface |
| `app/src/main/java/com/storebook/inventoryapp/data/play/PlayBillingManager.kt` | Google Play Billing subscription management |
| `app/src/main/java/com/storebook/inventoryapp/data/play/Purchases.kt` | Purchase data class |
| `app/src/main/java/com/storebook/inventoryapp/data/sync/SyncWorker.kt` | Push+Pull bidirectional sync via Data Connect |

### UI — Screens
| File | Purpose |
|---|---|
| `app/…/ui/screens/auth/AuthScreen.kt` | Phone OTP login, Google sign-in |
| `app/…/ui/screens/storebook/SplashScreen.kt` | App splash / loading |
| `app/…/ui/screens/storebook/OnboardingScreen.kt` | First-time language + store setup |
| `app/…/ui/screens/storebook/DashboardScreen.kt` | Main dashboard with stats, charts |
| `app/…/ui/screens/storebook/InventoryScreen.kt` | Item list, add/edit/delete, category filter |
| `app/…/ui/screens/storebook/SalesScreen.kt` | POS cart, checkout, discounts |
| `app/…/ui/screens/storebook/SalesHistoryScreen.kt` | Past sales list |
| `app/…/ui/screens/storebook/SalesAnalyticsScreen.kt` | Profit/loss analytics, date-range reports |
| `app/…/ui/screens/storebook/UdhaarScreen.kt` | Credit ledger (customer debts) |
| `app/…/ui/screens/storebook/MoreScreen.kt` | Settings, expenses, restock, export, theme |
| `app/…/ui/screens/storebook/QuotationScreen.kt` | Estimates / quotations list |
| `app/…/ui/screens/storebook/SupplierLedgerScreen.kt` | Supplier balances and purchase history |
| `app/…/ui/screens/storebook/GSTReportScreen.kt` | GST compliance report |
| `app/…/ui/screens/storebook/InviteStaffScreen.kt` | Staff account creation |
| `app/…/ui/screens/storebook/ProBillingView.kt` | Premium subscription UI |
| `app/…/ui/screens/storebook/DeleteConfirmationDialog.kt` | Reusable delete confirmation dialog |
| `app/…/ui/screens/storebook/SalesUtils.kt` | Sale formatting helpers |

### UI — Navigation
| File | Purpose |
|---|---|
| `app/…/ui/navigation/AppNavigation.kt` | NavHost composable with all route definitions |
| `app/…/ui/navigation/Routes.kt` | Sealed class defining all navigation routes |

### UI — Components
| File | Purpose |
|---|---|
| `app/…/ui/components/AlphabetScrubber.kt` | Alphabet fast-scroll sidebar |
| `app/…/ui/components/DynamicFastScroller.kt` | Dynamic section-based fast scroller |
| `app/…/ui/components/StoreBookAutocompleteDropdown.kt` | Reusable autocomplete dropdown |
| `app/…/ui/components/ads/` | Ad-related UI components |
| `app/…/ui/components/loader/` | Skeleton loaders |

### UI — ViewModels
| File | Purpose |
|---|---|
| `app/…/ui/viewmodels/StoreBookViewModel.kt` | Main ViewModel (47KB), all state management |
| `app/…/ui/viewmodels/AppConfigViewModel.kt` | Remote config / ad config ViewModel |

### UI — Theme
| File | Purpose |
|---|---|
| `app/…/ui/theme/` | Material3 theme definition files |

### Utilities
| File | Purpose |
|---|---|
| `app/…/utils/AnalyticsManager.kt` | Firebase Analytics event tracking |
| `app/…/utils/AppConfig.kt` | Build-time config constants |
| `app/…/utils/AppOpenAdManager.kt` | App-open ad lifecycle manager |
| `app/…/utils/AppUpdateHelper.kt` | In-app update helper (Play Core) |
| `app/…/utils/CurrencyUtils.kt` | Currency formatting (₹, locale-aware) |
| `app/…/utils/ExcelExporter.kt` | Excel/CSV export for inventory, sales, udhaar |
| `app/…/utils/InterstitialAdManager.kt` | Interstitial ad lifecycle manager |
| `app/…/utils/InvoicePdfGenerator.kt` | PDF invoice generation (iText-style) |
| `app/…/utils/LanguageManager.kt` | Multi-language support |
| `app/…/utils/LocaleHelper.kt` | Locale configuration helper |
| `app/…/utils/NetworkMonitor.kt` | Connectivity observer |
| `app/…/utils/ReviewUtils.kt` | In-app review prompt |
| `app/…/utils/SecurityUtils.kt` | EncryptedSharedPreferences wrapper |
| `app/…/utils/ShareUtils.kt` | Share intent builder |
| `app/…/utils/StringUtils.kt` | XSS sanitization for sync payloads |
| `app/…/utils/UdhaarPdfGenerator.kt` | Udhaar ledger PDF generation |

### Services & Workers
| File | Purpose |
|---|---|
| `app/…/services/MyFirebaseMessagingService.kt` | FCM push notification handler |
| `app/…/workers/ExpiryCheckWorker.kt` | Periodic batch-expiry check worker |

### Generated Data Connect SDK (Kotlin)
| File | Purpose |
|---|---|
| `app/…/dataconnect/com/…/` | Auto-generated Kotlin classes for all queries & mutations |

---

## Shared KMP Module (`shared/`)

| File | Purpose |
|---|---|
| `shared/src/commonMain/…/domain/models/Models.kt` | Domain model data classes (Item, Sale, CartItem, etc.) |
| `shared/src/commonMain/…/data/DatabaseDriverFactory.kt` | Expect declaration for SQLDelight driver |
| `shared/src/androidMain/…/data/DatabaseDriverFactory.kt` | Android actual SQLDelight driver |
| `shared/src/iosMain/…/data/DatabaseDriverFactory.kt` | iOS actual SQLDelight driver |
| `shared/src/commonMain/…/ui/components/loader/SkeletonLoader.kt` | Shared skeleton loader composable |

---

## Web App (`web/`)

### Root / Layout
| File | Purpose |
|---|---|
| `web/src/app/layout.tsx` | Root layout with Sidebar, ThemeProvider, StoreProvider |
| `web/src/app/page.tsx` | Dashboard page (SSR session check) |
| `web/src/app/globals.css` | Global CSS |
| `web/src/app/actions.ts` | Server Actions (login, logout, CRUD, admin ops) |

### Pages / Routes
| File | Purpose |
|---|---|
| `web/src/app/login/page.tsx` | Phone OTP + Staff login |
| `web/src/app/DashboardClient.tsx` | Dashboard client component |
| `web/src/app/DashboardCharts.tsx` | Chart components for dashboard |
| `web/src/app/ExportButtons.tsx` | CSV/PDF export buttons |
| `web/src/app/items/page.tsx` | Items page (SSR) |
| `web/src/app/items/ItemsClient.tsx` | Inventory CRUD client component |
| `web/src/app/sales/page.tsx` | Sales page (SSR) |
| `web/src/app/sales/SalesClient.tsx` | Sales history client component |
| `web/src/app/sales/SalesPOS.tsx` | Point-of-sale interface |
| `web/src/app/expenses/page.tsx` | Expenses page (SSR) |
| `web/src/app/expenses/ExpensesClient.tsx` | Expenses CRUD client component |
| `web/src/app/udhaar/page.tsx` | Udhaar page (SSR) |
| `web/src/app/udhaar/UdhaarClient.tsx` | Udhaar ledger client component |
| `web/src/app/reports/page.tsx` | Reports page (SSR) |
| `web/src/app/reports/ReportsClient.tsx` | Reports / analytics client component |
| `web/src/app/quotations/page.tsx` | Quotations page (SSR) |
| `web/src/app/quotations/QuotationsClient.tsx` | Quotations client component |
| `web/src/app/settings/page.tsx` | Settings page |
| `web/src/app/settings/ManageSubscription.tsx` | Subscription management |
| `web/src/app/settings/StaffManagement.tsx` | Staff account management |
| `web/src/app/settings/SubscriptionButton.tsx` | Subscription CTA button |

### Admin Pages
| File | Purpose |
|---|---|
| `web/src/app/admin/page.tsx` | Admin dashboard page (SSR) |
| `web/src/app/admin/AdminDashboardClient.tsx` | Admin overview client component |
| `web/src/app/admin/stores/page.tsx` | Stores management (SSR) |
| `web/src/app/admin/stores/StoresClient.tsx` | Store list & toggle client |
| `web/src/app/admin/users/page.tsx` | Users management (SSR) |
| `web/src/app/admin/users/UsersClient.tsx` | User list client |
| `web/src/app/admin/billing/page.tsx` | Billing management (SSR) |
| `web/src/app/admin/billing/BillingClient.tsx` | Billing/subscription admin client |
| `web/src/app/admin/settings/page.tsx` | Global settings (SSR) |
| `web/src/app/admin/settings/SettingsClient.tsx` | Global settings client |
| `web/src/app/admin/data/page.tsx` | Data management (SSR) |
| `web/src/app/admin/data/DataClient.tsx` | Data archival/purge client |

### Components
| File | Purpose |
|---|---|
| `web/src/components/Sidebar.tsx` | Navigation sidebar |
| `web/src/components/CreateStoreModal.tsx` | Create store modal dialog |
| `web/src/components/FormattedAmount.tsx` | Currency formatting component |
| `web/src/components/SetupProgress.tsx` | Post-login setup progress overlay |
| `web/src/components/ThemeProvider.tsx` | next-themes provider wrapper |
| `web/src/components/ThemeToggle.tsx` | Dark/light mode toggle |
| `web/src/components/models/RestockQuantity.tsx` | Restock quantity modal |

### Lib
| File | Purpose |
|---|---|
| `web/src/lib/firebase.ts` | Client-side Firebase init (Auth, DataConnect, RTDB) |
| `web/src/lib/firebaseAdmin.ts` | Server-side Firebase Admin init |
| `web/src/lib/session.ts` | Session verification + IDOR mitigation |
| `web/src/lib/constants.ts` | Country code list for phone auth |
| `web/src/lib/sanitize.ts` | XSS input sanitization |
| `web/src/lib/serializeDoc.ts` | Firestore doc serializer (legacy) |

### State Management (Redux)
| File | Purpose |
|---|---|
| `web/src/store/index.ts` | Redux store config with redux-persist |
| `web/src/store/StoreProvider.tsx` | Redux Provider wrapper |
| `web/src/store/cartSlice.ts` | Cart state slice |
| `web/src/store/inventorySlice.ts` | Inventory state slice |
| `web/src/store/udhaarSlice.ts` | Udhaar state slice |
| `web/src/proxy.ts` | DataConnect emulator proxy config |

### Generated Data Connect SDK (TypeScript)
| File | Purpose |
|---|---|
| `web/src/dataconnect/index.d.ts` | TypeScript type definitions |
| `web/src/dataconnect/index.cjs.js` | CommonJS module |
| `web/src/dataconnect/esm/` | ESM module directory |
| `web/src/dataconnect/package.json` | SDK package metadata |

---

## Firebase Data Connect / Backend (`dataconnect/`, `backend/`)

| File | Purpose |
|---|---|
| `dataconnect/dataconnect.yaml` | Service config (PostgreSQL connection) |
| `dataconnect/schema/schema.gql` | Full GraphQL schema (13 types) |
| `dataconnect/connector/connector.yaml` | SDK generation config (Kotlin + JS) |
| `dataconnect/connector/queries.gql` | All queries (22 queries) |
| `dataconnect/connector/mutations.gql` | All mutations (23 mutations) |
| `backend/server.js` | Legacy Express + SQLite REST API |
| `backend/package.json` | Backend dependencies |

---

## Project Root Config
| File | Purpose |
|---|---|
| `build.gradle.kts` | Root Gradle build |
| `settings.gradle.kts` | Gradle settings (multi-module) |
| `gradle.properties` | Gradle JVM + project properties |
| `firebase.json` | Firebase project config |
| `.firebaserc` | Firebase project aliases |
| `firestore.rules` | Firestore security rules |
| `.gitignore` | Git ignore rules |
| `.editorconfig` | Editor formatting config |

---

## Excluded Files (with reasons)

| Path / Pattern | Reason |
|---|---|
| `**/node_modules/` | Third-party dependencies (not project source) |
| `**/build/`, `*/build/` | Compiled build artifacts |
| `.gradle/`, `.idea/`, `.kotlin/` | IDE and build-system caches |
| `*.apk`, `*.aab`, `*.class`, `*.dex` | Compiled binaries |
| `app/google-services.json` | Firebase credentials (gitignored) |
| `web/.next/` | Next.js build output |
| `logcat*.txt`, `*.log` | Runtime debug logs |
| `*.db` (root-level) | Local SQLite database copies |
| `*.py` (root-level scripts) | One-off migration/fix scripts, not production code |
| `window_dump*.xml` | UI Automator dumps |
| `pdfbox_extracted/` | Extracted PDF assets |
| `web/src/dataconnect/README.md` | Auto-generated SDK docs |
| `localmcp/` | Local tooling config |
