# Loop State — StoreBook

Last run: 2025-08-01 (L1 report-only, daily triage)

## Gate Summary
| Gate | Status |
|------|--------|
| `./gradlew :app:ktlintCheck` | ✅ BUILD SUCCESSFUL |
| `./gradlew :shared:jvmTest` | ✅ BUILD SUCCESSFUL (27 tests) |
| `cd web && npm run lint` | ✅ Clean |
| `cd web && npm run type-check` | ✅ Clean |

## High Priority (waiting on human)

### Push & Open PR — `fix/sale-items-migration-2` @ `825f823`
**Status**: Commit ready locally. Cannot push — no git credentials / no `gh` CLI in this shell.
**Run these commands manually:**
```bash
git checkout fix/sale-items-migration-2
git push origin fix/sale-items-migration-2
gh pr create \
  --base master \
  --head fix/sale-items-migration-2 \
  --title "fix: add DB migration callback for sale_items.tax_rate and hsn_code columns" \
  --body '## What

Adds `DbMigrationCallback` — an idempotent \`onOpen\` callback that safely adds missing \`sale_items.tax_rate\` and \`sale_items.hsn_code\` columns on existing on-device databases via \`PRAGMA table_info\` introspection + \`ALTER TABLE ADD COLUMN\`.

Fixes \`upsertSaleItemRemote\` to include \`tax_rate\` and \`hsn_code\` columns so cloud-pulled sale items populate those fields correctly.

## Why

Existing device databases were created before \`tax_rate\`/`hsn_code\` were added to the \`sale_items\` table definition. SQLDelight version stayed at 1, so no migration fires automatically. App crashes with \`no such column: sale_items.tax_rate\` on cold start.

## Changes (6 files, 67 insertions)

| File | Change |
|------|--------|
| \`data/DbMigrationCallback.kt\` | New — idempotent migration callback |
| \`ui/viewmodel/ViewModelFactory.kt\` | Use shared \`DbMigrationCallback\` instead of inline callback |
| \`workers/ExpiryCheckWorker.kt\` | Wire migration callback through worker DB open path |
| \`data/sync/SyncWorker.kt\` | Wire migration callback + update call site for new params (defaults 0.0/null until cloud schema catches up) |
| \`shared/domain/repository/SyncRepository.kt\` | Add \`taxRate\`/`hsnCode\` params to \`upsertSaleItemWithCloudId\` |
| \`StoreBook.sq\` | Fix \`upsertSaleItemRemote\` INSERT OR REPLACE to include new columns |

## Testing

All pre-commit gates green: ktlint, shared:jvmTest (27 tests), web lint, web type-check, assembleDebug.'
```

## Watch List
- Husky pre-commit deprecation warning — remove shebang + husky.sh from `.husky/pre-commit` before v10 rollout.
- Dead branch `fix/sale-items-tax-rate-hsn-migration` & orphaned worktree ref — cleanup after merge.
- Cloud schema (`SyncSaleItemsQuery.kt`) lacks `taxRate`/`hsnCode` fields — call site passes defaults; should sync once cloud is updated.

## Recent Noise (ignored this run)
- None beyond above items.

---
Run log: L1 triage 2025-08-01 — all gates green, PR blocked on missing git credentials in opencode shell.
