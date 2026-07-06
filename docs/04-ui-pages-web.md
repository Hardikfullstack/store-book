# 04 — UI Pages (Web)

All pages use Next.js 14 App Router. Server components (`page.tsx`) fetch session data and pass it to client components (`*Client.tsx`). Styling uses Tailwind-like utility classes defined in `globals.css`. State management uses Redux Toolkit with `redux-persist` (localStorage).

---

## Root Layout
- **File**: `web/src/app/layout.tsx`
- **Purpose**: Global layout wrapping all pages.
- **Components**: `<ThemeProvider>` (next-themes), `<StoreProvider>` (Redux), `<Sidebar>` (navigation).
- **Session**: `getSession()` called server-side, passed to Sidebar for role-aware navigation.
- **Font**: Inter (Google Fonts).
- **PWA**: `<link rel="manifest" href="/manifest.json">`, `theme-color: #0d9488`.

---

## Login Page
- **Route**: `/login`
- **Files**: `login/page.tsx`
- **Auth modes**:

### Owner Login (Phone OTP)
1. Select country code (defaults to `+91` India).
2. Enter phone number → `signInWithPhoneNumber()` with invisible reCAPTCHA.
3. Enter 6-digit OTP → `confirmationResult.confirm(otp)`.
4. Get `idToken` → call server action `login(idToken)` → creates session cookie (5-day expiry).
5. Show `SetupProgress` overlay → redirect to `/`.

### Staff Login (Email/Password)
1. Enter username + PIN.
2. Derives virtual email: `{username.toLowerCase()}@storebook.internal`.
3. `signInWithEmailAndPassword(auth, virtualEmail, pin)`.
4. Same cookie flow as owner.

- **Validation**: Phone number strips non-digits. OTP requires exactly 6 digits. `sanitizeInput()` applied to all inputs.
- **Error handling**: Firebase error messages displayed inline. reCAPTCHA cleared on error.

---

## Dashboard Page
- **Route**: `/` (root)
- **Files**: `page.tsx` (SSR), `DashboardClient.tsx` (client), `DashboardCharts.tsx`, `ExportButtons.tsx`
- **Server-side**: Calls `getSession()`. Passes empty stats object (charts load client-side via Data Connect SDK).
- **Client-side**: Fetches data via generated TypeScript Data Connect queries (`getActiveItems`, `getActiveSales`, `getActiveSaleItems`, `getActiveUdhaars`, `getActiveExpenses`). Dispatches to Redux slices.
- **Display**: Summary stat cards, revenue chart, expense chart, recent sales, low-stock alerts, near-expiry warnings.
- **Sync**: Listens to Firebase RTDB `store_updates/$storeId/last_update` for real-time data refresh.
- **Export**: CSV/Excel export buttons via `ExportButtons.tsx`.

---

## Inventory Page
- **Route**: `/items`
- **Files**: `items/page.tsx` (SSR), `items/ItemsClient.tsx`
- **Data source**: `getActiveItems({ storeId })` via Data Connect SDK.
- **Features**:
  - Search (client-side filter on loaded data)
  - Category filter
  - Sort by name, price, quantity
  - Add/Edit/Delete items (via Data Connect mutations)
  - Restock via `RestockQuantity.tsx` modal → inserts expense + updates item qty
  - Low-stock indicators
- **Redux**: Items stored in `inventorySlice`.

---

## Sales Page
- **Route**: `/sales`
- **Files**: `sales/page.tsx` (SSR), `sales/SalesClient.tsx`, `sales/SalesPOS.tsx`
- **Data source**: `getActiveSales({ storeId, type: 'SALE' })`, `getActiveSaleItems({ storeId })`.
- **Features**:
  - POS interface: item picker, cart, discount, customer info, checkout
  - Sales history list with details
  - Bill PDF generation
- **Cart state**: Redux `cartSlice` (persisted to localStorage).

---

## Udhaar (Credit) Page
- **Route**: `/udhaar`
- **Files**: `udhaar/page.tsx` (SSR), `udhaar/UdhaarClient.tsx`
- **Data source**: `getActiveUdhaars({ storeId })`.
- **Features**:
  - Customer balance list
  - Individual customer ledger view
  - Add credit/payment entries
- **Redux**: `udhaarSlice`.

---

## Expenses Page
- **Route**: `/expenses`
- **Files**: `expenses/page.tsx` (SSR), `expenses/ExpensesClient.tsx`
- **Data source**: `getActiveExpenses({ storeId })`.
- **Features**: Expense list with type filter, add expense form (type, description, amount, supplier).

---

## Reports Page
- **Route**: `/reports`
- **Files**: `reports/page.tsx` (SSR), `reports/ReportsClient.tsx`
- **Purpose**: Analytics dashboard with date-range filters.
- **Data source**: Combines sales + expenses data from Redux/Data Connect.
- **Output**: Revenue, profit margins, expense breakdown, comparative charts.

---

## Quotations Page
- **Route**: `/quotations`
- **Files**: `quotations/page.tsx` (SSR), `quotations/QuotationsClient.tsx`
- **Data source**: `getActiveSales({ storeId, type: 'ESTIMATE' })`.
- **Features**: View estimates, convert to sale.

---

## Settings Page
- **Route**: `/settings`
- **Files**: `settings/page.tsx`, `settings/ManageSubscription.tsx`, `settings/StaffManagement.tsx`, `settings/SubscriptionButton.tsx`
- **Features**:
  - Business profile editing
  - Staff account management (create/revoke)
  - Subscription status view
  - Theme toggle

---

## Admin Pages

### Admin Dashboard
- **Route**: `/admin`
- **Files**: `admin/page.tsx`, `admin/AdminDashboardClient.tsx`
- **Access**: `role === 'admin' || 'super_admin'` (checked in session).
- **Features**: Total stores, total users, system health overview.

### Store Management
- **Route**: `/admin/stores`
- **Files**: `admin/stores/page.tsx`, `admin/stores/StoresClient.tsx`
- **Data source**: `getStoresPaginated()` server action.
- **Features**: List stores, toggle active/inactive status via `toggleStoreStatus()`.

### User Management
- **Route**: `/admin/users`
- **Files**: `admin/users/page.tsx`, `admin/users/UsersClient.tsx`
- **Data source**: `getUsersPaginated()` server action.
- **Features**: List users, view roles, revoke sessions.

### Billing Management
- **Route**: `/admin/billing`
- **Files**: `admin/billing/page.tsx`, `admin/billing/BillingClient.tsx`
- **Features**: View subscriptions, manage promo codes.

### Data Management
- **Route**: `/admin/data`
- **Files**: `admin/data/page.tsx`, `admin/data/DataClient.tsx`
- **Features**: Data archival (`archiveOldData()`), store purge (`purgeStoreData()`), audit log viewer.

### Global Settings
- **Route**: `/admin/settings`
- **Files**: `admin/settings/page.tsx`, `admin/settings/SettingsClient.tsx`
- **Features**: System-wide config (GlobalSetting table), announcements management.

---

## Shared Components

| Component | File | Purpose |
|---|---|---|
| Sidebar | `components/Sidebar.tsx` | Navigation sidebar with role-aware menu items |
| ThemeToggle | `components/ThemeToggle.tsx` | Dark/light/system theme toggle |
| ThemeProvider | `components/ThemeProvider.tsx` | next-themes wrapper |
| CreateStoreModal | `components/CreateStoreModal.tsx` | Modal dialog for creating new store |
| FormattedAmount | `components/FormattedAmount.tsx` | Currency formatting (₹ symbol) |
| SetupProgress | `components/SetupProgress.tsx` | Post-login loading overlay |
| RestockQuantity | `components/models/RestockQuantity.tsx` | Restock quantity entry modal |

---

## Server Actions (`actions.ts`)

| Action | Auth Required | Purpose |
|---|---|---|
| `login(idToken)` | No | Creates session cookie from Firebase ID token |
| `logout()` | No | Deletes session + activeStoreId cookies |
| `switchStore(storeId)` | Yes (owner/admin) | Sets activeStoreId cookie with IDOR validation |
| `createStaffAccount(...)` | Yes (owner) | Creates Firebase Auth user + Data Connect User record |
| `getStoresPaginated()` | Implicit | Lists all stores (admin) |
| `getUsersPaginated()` | Implicit | Lists all users (admin) |
| `toggleStoreStatus(...)` | Yes (admin) | Activates/deactivates a store |
| `purgeStoreData(storeId)` | Yes (admin) | Soft-deletes store + audit log |
| `revokeUserSessions(userId)` | Yes (admin) | Revokes Firebase Auth refresh tokens |
| `archiveOldData(daysOld)` | Implicit | Simulated archival (returns mock count) |
| `createStore(name)` | Implicit | **NOT IMPLEMENTED** — returns error |
| `fetchMoreData(...)` | Implicit | Returns empty array (pagination stub) |
| `updateUserRole(...)` | Implicit | **NOT IMPLEMENTED** — empty function body |

---

## Redux State Shape

```typescript
{
  cart: {
    items: CartItem[],    // { item: Item, quantity: number }
    discount: number,
    customerName: string,
  },
  inventory: {
    items: Item[],
    loading: boolean,
    lastFetched: number,
  },
  udhaar: {
    entries: UdhaarEntry[],
    loading: boolean,
  }
}
```

**Persistence**: All slices persisted to `localStorage` under key `storebook-root`.
