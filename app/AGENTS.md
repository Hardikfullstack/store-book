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

## Before finishing
Run in order: `./gradlew assembleDebug` → `./gradlew :app:ktlintCheck` → `./gradlew :shared:jvmTest` (if shared logic touched) → `./gradlew verifyRoborazziDebug` (if UI touched).
