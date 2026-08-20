# app/ — Android (Kotlin/Compose)

Scope: `app/**`

## Structure
- Compose UI screens live under `ui/`; one ViewModel per screen, following the existing `{Screen}ViewModel` naming.
- SQLite access goes through SQLDelight-generated `StoreBookDatabase` (schema lives in `:shared`, not here) — never write raw JDBC/Cursor code.
- `SyncWorker` (in `services/` or `workers/`) is the only place that talks to Firebase Data Connect from Android; apply the Retry defensive pattern here.

## Conventions
- Package root: `com.storebook.inventoryapp.{data,ui,services,workers}`.
- Every query/mutation touching a tenant-scoped table must filter/set `storeId` — no exceptions.

## Testing & Snapshots
- Unit tests for shared logic run via `:shared:jvmTest`, not from `app/`.
- Roborazzi golden images live in `app/src/test/snapshots/` and are committed to the repo. 0.5% pixel-diff threshold.
- After any Compose UI change: run `./gradlew verifyRoborazziDebug`. If the diff is an intentional visual change, regenerate with `./gradlew recordRoborazziDebug` and call this out explicitly — never regenerate to silence a real regression.

## Code Review Checklist
When reviewing or self-checking a change in `app/**`, go through these in order:

1. **Functional correctness** — does the change do what the task/PR describes, including on real device/emulator behavior, not just compiling.
2. **Bugs and edge cases** — nulls, empty lists, rotation/process-death state restoration, offline/no-network paths, `SyncWorker` retry/backoff and partial-failure handling.
3. **Tenant isolation (security)** — every touched query/mutation on a tenant-scoped table sets/filters `storeId`; no path where one store's data can leak into another's UI or sync payload.
4. **SRP** — ViewModels stay screen-specific business logic only; no SQLDelight or Firebase calls leaking into Composables; no `SyncWorker` doing UI-adjacent work.
5. **OCP / DIP** — sync and data-access logic is extendable (new entity types, new sync targets) without modifying `SyncWorker`'s core retry loop; ViewModels depend on repository/interface abstractions from `:shared`, not concrete SQLDelight or Firebase Data Connect types.
6. **ISP** — repository/data-source interfaces exposed to ViewModels aren't bloated with methods only `SyncWorker` needs, or vice versa.
7. **Coupling and cohesion** — `ui/`, `services/`/`workers/`, and `data/` stay cleanly separated per the package-root convention; avoid cross-package reach-ins.
8. **Naming** — `{Screen}ViewModel` convention honored; package placement (`data`/`ui`/`services`/`workers`) matches responsibility.
9. **Error handling** — Firebase Data Connect failures in `SyncWorker` are retried per the defensive pattern, not swallowed; user-facing errors surfaced sensibly from ViewModels, not left as silent Compose recomposition gaps.
10. **Testability** — logic that can live in `:shared` (and be covered by `:shared:jvmTest`) isn't stranded in `app/` where it can't be unit tested.
11. **Missing tests** — new shared logic has `:shared:jvmTest` coverage; new/changed UI has a Roborazzi snapshot; sync retry paths have a test for the failure branch, not just the happy path.
12. **Performance** — unnecessary recomposition (unstable params, missing `remember`/keys), N+1-style SQLDelight query patterns, unbounded work inside `SyncWorker`.
13. **Unnecessary complexity / duplication** — no duplicated storeId-filtering logic, duplicated retry logic outside `SyncWorker`, or over-engineered abstractions for a single call site.
14. **Architecture fit** — change respects the existing layering (Compose UI → ViewModel → `:shared` repository → SQLDelight/Firebase); flag anything that reintroduces raw JDBC/Cursor or bypasses `SyncWorker` for Data Connect access.

## Before finishing
Run in order: `./gradlew assembleDebug` → `./gradlew :app:ktlintCheck` → `./gradlew :shared:jvmTest` (if shared logic touched) → `./gradlew verifyRoborazziDebug` (if UI touched).
