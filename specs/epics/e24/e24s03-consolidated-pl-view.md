# E24-S3: Consolidated P&L View Across All Owned Stores

## 1. Story ID
`e24-s3`

## 2. Story Title
Consolidated P&L view across all owned stores

## 3. Epic Reference
- **Epic**: [E24 — Multi-Store Dashboard & Cross-Store Analytics](./epic.yaml)
- **Story sequence**: e24-s1 (store switcher with names) → e24-s2 (cross-store charts) → **e24-s3** (consolidated P&L) → e24-s4 (Android store switching)

## 4. Story Type
`feat` — New feature within existing dashboard infrastructure

## 5. Risk Level
**P1** — Core financial aggregation logic, cross-store data access, multi-module changes

## 6. Context
domain: Financial dashboard / multi-tenancy visualization

## 7. Description
Owners with multiple stores currently must view P&L data one store at a time. There is no way to see an aggregated overview of total revenue, expenses, and net profit across all owned stores simultaneously. This story adds a consolidated P&L dashboard accessible via a new "All Stores" option in the existing store switcher.

## 8. User Story
> As an owner who runs multiple stores, I want to see a single dashboard showing my total revenue, expenses, and profit across ALL my stores so I can understand my overall business health at a glance without switching between individual store views.

## 9. Business Context
Multi-store owners making daily operational decisions need a consolidated financial view. Without this, they must mentally aggregate data from multiple store views or use external spreadsheets. This feature is part of the v1.7.0 Multi-Store Dashboard epic that positions StoreBook as a true multi-location management platform.

## 10. Acceptance Criteria
1. New "All Stores" option appears in store switcher dropdown for users with `role === 'owner'` and multiple stores
2. Selecting "All Stores" loads consolidated financial data via server action
3. Total Revenue card shows sum of all sales totalAmount across owned stores for selected date range
4. Total Expenses card shows sum of all expense amounts across owned stores for selected date range
5. Net P&L card shows correct calculation (revenue - expenses), green if positive, red if negative
6. Date range selector controls all aggregated values consistently (today / week / month / all)
7. Per-store breakdown table shows individual store revenue, expenses, and profit rows
8. Clicking a summary card toggles drill-down to per-store detail view
9. Numbers are accurate within ₹0.01 of manual sum verification
10. Switching from "All Stores" back to a specific store restores normal single-store dashboard

## 11. Requirements

### ADDED: Consolidated financial overview
Users can access an aggregated P&L dashboard showing total revenue, total expenses, and net profit summed across all their owned stores for a selected date range.

### ADDED: Per-store breakdown table
The consolidated view includes a drill-down table showing each store's individual contribution to the aggregate numbers (store name, revenue, expenses, profit).

### MODIFIED: Store switcher behavior
**Before:** Store switcher dropdown shows only individual store options and "Create New Store"
**After:** Store switcher also includes "All Stores (Consolidated)" at the top of the list. Selecting it sets a sentinel value ("all") that triggers consolidated data loading instead of per-store data.

### MODIFIED: Dashboard rendering logic
**Before:** Dashboard always displays single-store financial data based on `activeStoreId` from session cookie
**After:** Dashboard detects `storeId === "all"` and conditionally renders the consolidated P&L view with aggregated stats and drill-down tables. Single-store view is preserved when a specific storeId is selected.

### MODIFIED: Server data fetching
**Before:** page.tsx passes minimal initialStats directly to DashboardClient which handles all data loading client-side via DataConnect delta sync
**After:** When `activeStoreId === "all"` and role is owner, page.tsx fetches consolidated data from new server action `getConsolidatedPLData()` before passing it to DashboardClient.

## 12. Technical Approach
- **Server action** (`actions.ts`): `getConsolidatedPLData(storeIds[], startDate, endDate)` loops over user's owned storeIds, calls `dc.executeGraphql()` with inline GetActiveSales + GetActiveExpenses queries per store, aggregates totals server-side. Returns `{ consolidated: { totalRevenue, totalExpenses, netPL }, perStore: [{ storeId, revenue, expenses, profit }] }`.
- **Page.tsx modification**: Detect `session.activeStoreId === "all"`, call `getConsolidatedPLData(session.stores, startTs, endTs)` server-side, pass consolidated data to DashboardClient as new prop or initialStats extension.
- **DashboardClient mode switch**: Accept new boolean `isConsolidatedMode` prop. When true, render inline summary cards and drill-down table instead of per-store stat cards. Date range selector remains shared.
- **New component** (`ConsolidatedPLView.tsx`): Reusable component rendering three P&L summary cards with click-to-drill-down toggle, a per-store breakdown `<table>`, and a shared date-range filter using existing pattern from DashboardClient.
- **Sidebar** (`Sidebar.tsx`): Insert `<option value="all">All Stores (Consolidated)</option>` as first option in the dropdown for owners with `stores.length >= 1`.

## 13. Dependencies
- **e24-s1**: Store switcher with store names — assumed complete or acceptable to use current "Store N" format temporarily with name lookup via GetStore query
- **Existing DataConnect queries**: GetActiveSales, GetActiveExpenses already support per-storeId filtering with startDate/endDate params

## 14. Related Stories
- e24-s2 (Cross-store comparison chart) — parallel effort, same epic; shares some data aggregation logic but serves different UI purpose
- e24-s4 (Android store switching) — separate platform, no direct code overlap

## 15. Notes
- No DataConnect schema changes required — all needed data exists per-storeId
- IDOR protection already handled by `switchStore()` validating ownership; consolidate action must validate similarly
- Store name display: use GetStore query to fetch names for breakdown table, fallback to "My Store" if null/empty
- Large store count (10+) handled by Promise.all parallel queries with reasonable timeout

## 16. Blocked By
Nothing — all required data and infrastructure exists. e24-s1 is a nice-to-have dependency but not blocking.

## 17. Definition of Done
- [ ] Server action `getConsolidatedPLData` returns correct aggregated data for test stores
- [ ] "All Stores" option visible in store switcher for multi-store owners
- [ ] Consolidated P&L view renders with accurate numbers
- [ ] Per-store breakdown table rows sum to match card totals within ₹0.01
- [ ] Date range filter controls all values consistently
- [ ] Edge cases handled (1 store, 0 data, empty periods)
- [ ] `npm run lint && npm run build` passes with no errors or new warnings
- [ ] Switching back to single-store view works correctly

## 18. Effort Estimate
6-8 hours total across 4 vertical slices (3 BCPs)

## 19. Out of Scope
- Real-time cross-store sync (sync remains per-store; consolidated reads from cloud on navigation)
- Multi-store comparison charts (that's e24-s2)
- Android native implementation (that's e24-s4)
- GSTR-2 export functionality
- Role-based access changes (existing permission model preserved)
- Dark mode specific adaptations (existing theme classes apply automatically)
- Store name validation (epic hard requirement, not re-validated here)

## 20. Risks
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Aggregated numbers deviate from manual sums due to date boundary handling | Medium | High | Use Float timestamps consistently with existing query patterns; verify edge cases in TDD phase |
| Performance degradation with 10+ stores (many parallel DataConnect queries) | Low | Medium | Promise.all with server-side timeout; add loading state UX |
| Breaking existing DashboardClient delta-sync when adding mode switch | Medium | Medium | Wrap consolidated path in conditional branch; keep existing sync logic for single-store mode unchanged |
| storeId sentinel value "all" conflicts with actual store ID | Very Low | Critical | Store IDs are UUIDs (RFC 4122); "all" cannot collide. Document this invariant. |
